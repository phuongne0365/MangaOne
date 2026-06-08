package com.mangaone.service.impl;

import com.mangaone.entity.Manga;
import com.mangaone.entity.Order;
import com.mangaone.entity.OrderDetail;
import com.mangaone.repository.MangaRepository;
import com.mangaone.repository.OrderRepository;
import com.mangaone.service.ExcelReportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelReportServiceImpl implements ExcelReportService {

    // ─── Bảng màu chủ đạo Navy (xuyên suốt 4 sheet) ─────────────────────────
    private static final XSSFColor COLOR_NAVY        = fromHex("1B3A6B"); // Header cột
    private static final XSSFColor COLOR_NAVY_LIGHT  = fromHex("D6E0F0"); // Header info (dòng 1-4)
    private static final XSSFColor COLOR_ACCENT      = fromHex("2E75B6"); // Sub-label section
    private static final XSSFColor COLOR_WHITE       = fromHex("FFFFFF");
    private static final XSSFColor COLOR_ROW_ALT     = fromHex("EFF4FB"); // Zebra stripe
    private static final XSSFColor COLOR_KPI_BG      = fromHex("F0F5FF"); // Nền KPI box
    private static final XSSFColor COLOR_WARN_RED_BG = fromHex("FFE0E0");
    private static final XSSFColor COLOR_WARN_ORA_BG = fromHex("FFF3CD");

    private static final String COMPANY_NAME = "MangaOne";

    private static XSSFColor fromHex(String hex) {
        Color c = Color.decode("#" + hex);
        return new XSSFColor(new byte[]{(byte) c.getRed(), (byte) c.getGreen(), (byte) c.getBlue()}, null);
    }

    // ─── Repositories ─────────────────────────────────────────────────────────
    private final OrderRepository orderRepository;
    private final MangaRepository mangaRepository;

    public ExcelReportServiceImpl(OrderRepository orderRepository, MangaRepository mangaRepository) {
        this.orderRepository = orderRepository;
        this.mangaRepository = mangaRepository;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PUBLIC API
    // ═════════════════════════════════════════════════════════════════════════
    @Override
    public byte[] generateReport(LocalDate startDate, LocalDate endDate, String reporterName) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            List<Order> allOrders   = orderRepository.findAll();
            List<Manga> allMangas   = mangaRepository.findAll();
            List<Order> filtered    = filterOrdersByDate(allOrders, startDate, endDate);

            createSheet1(workbook, filtered,  startDate, endDate, reporterName);
            createSheet2(workbook, allMangas, allOrders, reporterName);
            createSheet3(workbook, filtered,  startDate, endDate, reporterName);
            createSheet4(workbook, allMangas, filtered, allOrders, startDate, endDate, reporterName);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xuất file Excel báo cáo", e);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HEADER INFO  (dòng 0-4, dùng chung cho tất cả sheet)
    // ═════════════════════════════════════════════════════════════════════════
    /**
     * Tạo 5 dòng header thống nhất:
     *   0 – Tên công ty  (navy light bg, bold 11)
     *   1 – Tiêu đề báo cáo (navy bg, white, bold 16, ALL CAPS)
     *   2 – Kỳ báo cáo  (italic, center)
     *   3 – Người lập   (italic, center)
     *   4 – Thời gian xuất (italic, center)
     */
    private void createHeaderInfo(XSSFSheet sheet, XSSFWorkbook wb,
                                  String title, int numCols,
                                  LocalDate startDate, LocalDate endDate,
                                  String reporterName) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // ── Row 0: Company name ──────────────────────────────────────────────
        Row r0 = sheet.createRow(0);
        r0.setHeightInPoints(20);
        Cell c0 = r0.createCell(0);
        c0.setCellValue(COMPANY_NAME.toUpperCase() + " — BÁO CÁO NỘI BỘ");
        c0.setCellStyle(buildCompanyNameStyle(wb, numCols));
        merge(sheet, 0, 0, 0, numCols - 1);

        // ── Row 1: Report title ──────────────────────────────────────────────
        Row r1 = sheet.createRow(1);
        r1.setHeightInPoints(32);
        Cell c1 = r1.createCell(0);
        c1.setCellValue(title.toUpperCase());
        c1.setCellStyle(buildTitleStyle(wb));
        merge(sheet, 1, 1, 0, numCols - 1);

        // ── Styles cho dòng meta (2-4) ───────────────────────────────────────
        CellStyle metaStyle = buildMetaStyle(wb);

        // ── Row 2: Period ────────────────────────────────────────────────────
        String periodStr;
        if (startDate != null && endDate != null) {
            periodStr = "Kỳ báo cáo: Từ ngày " + startDate.format(df) + " đến ngày " + endDate.format(df);
        } else if (startDate != null) {
            periodStr = "Kỳ báo cáo: Từ ngày " + startDate.format(df);
        } else if (endDate != null) {
            periodStr = "Kỳ báo cáo: Đến ngày " + endDate.format(df);
        } else {
            periodStr = "Kỳ báo cáo: Toàn thời gian";
        }
        Row r2 = sheet.createRow(2);
        r2.setHeightInPoints(18);
        setMeta(r2, 0, periodStr, metaStyle, sheet, numCols);

        // ── Row 3: Reporter ──────────────────────────────────────────────────
        Row r3 = sheet.createRow(3);
        r3.setHeightInPoints(18);
        setMeta(r3, 0, "Người lập báo cáo: " + reporterName, metaStyle, sheet, numCols);

        // ── Row 4: Export time ───────────────────────────────────────────────
        Row r4 = sheet.createRow(4);
        r4.setHeightInPoints(18);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        setMeta(r4, 0, "Thời gian xuất báo cáo: " + now, metaStyle, sheet, numCols);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SHEET 1 – TỔNG QUAN & DOANH THU
    // ═════════════════════════════════════════════════════════════════════════
    private void createSheet1(XSSFWorkbook wb, List<Order> filteredOrders,
                              LocalDate startDate, LocalDate endDate,
                              String reporterName) {
        String[] columns = {"STT", "Mã Truyện", "Tên truyện", "Danh mục",
                "Nhà Xuất Bản", "Số lượng bán", "Doanh thu (VNĐ)"};
        XSSFSheet sheet = wb.createSheet("TỔNG QUAN & DOANH THU");
        applyPrintSetup(sheet, columns.length);

        createHeaderInfo(sheet, wb, "BÁO CÁO TỔNG QUAN & DOANH THU", columns.length,
                startDate, endDate, reporterName);

        // ── KPI counters ─────────────────────────────────────────────────────
        long totalRevenue    = 0;
        int  countSuccess    = 0;
        int  countProcessing = 0;
        int  countCanceled   = 0;
        Map<Long, Map<String, Object>> mangaStats = new HashMap<>();

        for (Order o : filteredOrders) {
            boolean ok  = "DELIVERED".equals(o.getStatus()) || "COMPLETED".equals(o.getStatus());
            boolean proc = "PENDING".equals(o.getStatus()) || "CONFIRMED".equals(o.getStatus()) || "SHIPPING".equals(o.getStatus());
            boolean can  = "CANCELLED".equals(o.getStatus());

            if (ok)   { countSuccess++;   if (o.getTotalAmount() != null) totalRevenue += o.getTotalAmount(); }
            else if (proc) countProcessing++;
            else if (can)  countCanceled++;

            if (ok && o.getOrderDetails() != null) {
                for (OrderDetail d : o.getOrderDetails()) {
                    Manga m = d.getManga();
                    if (m == null) continue;
                    Map<String, Object> s = mangaStats.computeIfAbsent(m.getId(), k -> {
                        Map<String, Object> mp = new HashMap<>();
                        mp.put("manga", m); mp.put("quantity", 0); mp.put("revenue", 0L);
                        return mp;
                    });
                    int  qty = d.getQuantity() != null ? d.getQuantity() : 0;
                    long prc = d.getPrice()    != null ? d.getPrice()    : 0;
                    s.put("quantity", (int)  s.get("quantity") + qty);
                    s.put("revenue",  (long) s.get("revenue")  + prc * qty);
                }
            }
        }

        // ── KPI Box (row 5-7) ────────────────────────────────────────────────
        CellStyle kpiBox = buildKpiStyle(wb);
        int totalOrders = countSuccess + countProcessing + countCanceled;

        createKpiRow(sheet, wb, 5, columns.length,
                "💰  Tổng doanh thu (đơn Đã giao + Hoàn thành): "
                        + String.format("%,d", totalRevenue) + " VNĐ", kpiBox, true);
        createKpiRow(sheet, wb, 6, columns.length,
                "📦  Tổng đơn hàng: " + totalOrders
                        + "   |   ✅ Thành công: " + countSuccess
                        + "   |   ⏳ Đang xử lý: " + countProcessing
                        + "   |   ❌ Đã hủy: " + countCanceled, kpiBox, false);

        // ── Table ────────────────────────────────────────────────────────────
        int tableStart = 8;
        createTableHeader(sheet, wb, tableStart, columns);

        List<Map<String, Object>> sorted = new ArrayList<>(mangaStats.values());
        sorted.sort((a, b) -> Integer.compare((int) b.get("quantity"), (int) a.get("quantity")));

        CellStyle dataStyle   = buildDataStyle(wb, false);
        CellStyle dataAlt     = buildDataStyle(wb, true);
        CellStyle numStyle    = buildNumStyle(wb, false);
        CellStyle numAlt      = buildNumStyle(wb, true);

        int rowIdx = tableStart + 1;
        int stt = 1;
        for (Map<String, Object> stat : sorted) {
            boolean alt = (stt % 2 == 0);
            Row row = sheet.createRow(rowIdx++);
            row.setHeightInPoints(18);
            Manga m = (Manga) stat.get("manga");
            fillCells(row, columns.length, alt ? dataAlt : dataStyle);
            row.getCell(0).setCellValue(stt++);
            row.getCell(1).setCellValue(str(m.getId()));
            row.getCell(2).setCellValue(str(m.getTitle()));
            row.getCell(3).setCellValue(m.getCategory() != null ? m.getCategory().getCategoryName() : "");
            row.getCell(4).setCellValue(m.getPublisher() != null ? m.getPublisher().getPublisherName() : "");
            row.getCell(5).setCellStyle(alt ? numAlt : numStyle);
            row.getCell(5).setCellValue((int) stat.get("quantity"));
            row.getCell(6).setCellStyle(alt ? numAlt : numStyle);
            row.getCell(6).setCellValue((long) stat.get("revenue"));
        }

        autoSize(sheet, columns.length);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SHEET 2 – TỒN KHO & VẬN HÀNH
    // ═════════════════════════════════════════════════════════════════════════
    private void createSheet2(XSSFWorkbook wb, List<Manga> allMangas,
                              List<Order> allOrders, String reporterName) {
        String[] columns = {"STT", "ID Truyện", "Tên truyện", "Nhà xuất bản",
                "Thể loại", "Giá bán (VNĐ)", "Tồn kho", "Mức độ cảnh báo"};
        XSSFSheet sheet = wb.createSheet("TỒN KHO & VẬN HÀNH");
        applyPrintSetup(sheet, columns.length);

        createHeaderInfo(sheet, wb, "BÁO CÁO TỒN KHO & VẬN HÀNH",
                columns.length, null, null, reporterName);

        // Last-sale map
        Map<Long, LocalDate> lastSaleDates = buildLastSaleMap(allOrders);

        int tableStart = 5;
        createTableHeader(sheet, wb, tableStart, columns);

        CellStyle dataStyle = buildDataStyle(wb, false);
        CellStyle dataAlt   = buildDataStyle(wb, true);
        CellStyle numStyle  = buildNumStyle(wb, false);
        CellStyle numAlt    = buildNumStyle(wb, true);
        // Warning cell styles
        CellStyle warnRed = buildWarningCellStyle(wb, COLOR_WARN_RED_BG, IndexedColors.RED);
        CellStyle warnOra = buildWarningCellStyle(wb, COLOR_WARN_ORA_BG, IndexedColors.DARK_YELLOW);

        int rowIdx = tableStart + 1;
        int stt = 1;
        for (Manga m : allMangas) {
            boolean alt  = (stt % 2 == 0);
            Row row = sheet.createRow(rowIdx++);
            row.setHeightInPoints(18);
            fillCells(row, columns.length, alt ? dataAlt : dataStyle);

            int stock = m.getStockQuantity() != null ? m.getStockQuantity() : 0;
            LocalDate lastSale = lastSaleDates.get(m.getId());

            String warning;
            CellStyle warnStyle;
            if (stock == 0) {
                warning = "⛔ Hết sạch";
                warnStyle = warnRed;
            } else if (lastSale != null && lastSale.isBefore(LocalDate.now().minusMonths(3))) {
                warning = "⚠️ Dead stock (> 3 tháng)";
                warnStyle = warnRed;
            } else if (stock < 10) {
                warning = "⚡ Sắp hết hàng (< 10)";
                warnStyle = warnOra;
            } else {
                warning = "✅ Bình thường";
                warnStyle = alt ? dataAlt : dataStyle;
            }

            row.getCell(0).setCellValue(stt++);
            row.getCell(1).setCellValue(str(m.getId()));
            row.getCell(2).setCellValue(str(m.getTitle()));
            row.getCell(3).setCellValue(m.getPublisher() != null ? m.getPublisher().getPublisherName() : "");
            row.getCell(4).setCellValue(m.getCategory() != null ? m.getCategory().getCategoryName() : "");
            row.getCell(5).setCellStyle(alt ? numAlt : numStyle);
            row.getCell(5).setCellValue(m.getPrice() != null ? m.getPrice() : 0.0);
            row.getCell(6).setCellValue(stock);
            row.getCell(7).setCellValue(warning);
            row.getCell(7).setCellStyle(warnStyle);
        }

        autoSize(sheet, columns.length);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SHEET 3 – VẬN HÀNH ĐƠN HÀNG  (fix row height + wrap)
    // ═════════════════════════════════════════════════════════════════════════
    private void createSheet3(XSSFWorkbook wb, List<Order> filteredOrders,
                              LocalDate startDate, LocalDate endDate,
                              String reporterName) {
        String[] columns = {"STT", "Mã Đơn Hàng", "Ngày đặt", "Tên người nhận",
                "Số điện thoại", "Địa chỉ giao hàng", "Chi tiết sản phẩm",
                "Trạng thái", "Tổng thanh toán (VNĐ)"};
        XSSFSheet sheet = wb.createSheet("VẬN HÀNH ĐƠN HÀNG");
        applyPrintSetup(sheet, columns.length);

        createHeaderInfo(sheet, wb, "BÁO CÁO VẬN HÀNH ĐƠN HÀNG",
                columns.length, startDate, endDate, reporterName);

        int tableStart = 5;
        createTableHeader(sheet, wb, tableStart, columns);

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        CellStyle dataStyle  = buildDataStyle(wb, false);
        CellStyle dataAlt    = buildDataStyle(wb, true);
        CellStyle wrapStyle  = buildWrapStyle(wb, false);
        CellStyle wrapAlt    = buildWrapStyle(wb, true);
        CellStyle numStyle   = buildNumStyle(wb, false);
        CellStyle numAlt     = buildNumStyle(wb, true);

        // Fix column width for "Chi tiết sản phẩm" (col 6) to avoid auto-size overflow
        sheet.setColumnWidth(6, 40 * 256);

        int rowIdx = tableStart + 1;
        int stt = 1;
        for (Order o : filteredOrders) {
            boolean alt = (stt % 2 == 0);

            // Calculate row height based on number of product lines
            int productLines = 1;
            String productDetails = "";
            if (o.getOrderDetails() != null && !o.getOrderDetails().isEmpty()) {
                List<String> lines = o.getOrderDetails().stream()
                        .filter(d -> d.getManga() != null)
                        .map(d -> d.getManga().getTitle() + "  ×" + d.getQuantity())
                        .collect(Collectors.toList());
                productDetails = String.join("\n", lines);
                productLines = Math.max(1, lines.size());
            }
            // Each line ≈ 16pt, min 20pt
            float rowHeight = Math.max(20f, productLines * 16f);

            Row row = sheet.createRow(rowIdx++);
            row.setHeightInPoints(rowHeight);

            fillCells(row, columns.length, alt ? dataAlt : dataStyle);

            String dateStr = o.getCreatedAt() != null ? o.getCreatedAt().format(df) : "";

            row.getCell(0).setCellValue(stt++);
            row.getCell(1).setCellValue(str(o.getOrderId()));
            row.getCell(2).setCellValue(dateStr);
            row.getCell(3).setCellValue(str(o.getReceiverName()));
            row.getCell(4).setCellValue(str(o.getReceiverPhone()));
            row.getCell(5).setCellValue(str(o.getShippingAddress()));

            // "Chi tiết sản phẩm" — wrap text, căn top-left
            row.getCell(6).setCellStyle(alt ? wrapAlt : wrapStyle);
            row.getCell(6).setCellValue(productDetails);

            row.getCell(7).setCellValue(o.getStatus() != null ? getStatusVn(o.getStatus()) : "");
            row.getCell(8).setCellStyle(alt ? numAlt : numStyle);
            row.getCell(8).setCellValue(o.getTotalAmount() != null ? o.getTotalAmount() : 0);
        }

        // Auto-size all columns except col 6 (already fixed)
        for (int i = 0; i < columns.length; i++) {
            if (i != 6) sheet.autoSizeColumn(i);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SHEET 4 – BÁN CHẠY & BÁN Ế
    // ═════════════════════════════════════════════════════════════════════════
    private void createSheet4(XSSFWorkbook wb, List<Manga> allMangas,
                              List<Order> filteredOrders, List<Order> allOrders,
                              LocalDate startDate, LocalDate endDate,
                              String reporterName) {
        String[] colTop = {"STT", "Mã Truyện", "Tên truyện", "Danh mục",
                "Nhà Xuất Bản", "Số lượng bán", "Doanh thu (VNĐ)"};
        String[] colBad = {"STT", "Mã Truyện", "Tên truyện", "Danh mục",
                "Nhà Xuất Bản", "Tồn kho", "Bán gần nhất", "Tình trạng"};
        int numCols = 8;

        XSSFSheet sheet = wb.createSheet("BÁN CHẠY & BÁN Ế");
        applyPrintSetup(sheet, numCols);

        createHeaderInfo(sheet, wb, "THỐNG KÊ TRUYỆN BÁN CHẠY & BÁN Ế",
                numCols, startDate, endDate, reporterName);

        // ── Build stats ──────────────────────────────────────────────────────
        Map<Long, Map<String, Object>> mangaStats = new HashMap<>();
        for (Order o : filteredOrders) {
            boolean ok = "DELIVERED".equals(o.getStatus()) || "COMPLETED".equals(o.getStatus());
            if (ok && o.getOrderDetails() != null) {
                for (OrderDetail d : o.getOrderDetails()) {
                    Manga m = d.getManga();
                    if (m == null) continue;
                    Map<String, Object> s = mangaStats.computeIfAbsent(m.getId(), k -> {
                        Map<String, Object> mp = new HashMap<>();
                        mp.put("manga", m); mp.put("quantity", 0); mp.put("revenue", 0L);
                        return mp;
                    });
                    int  qty = d.getQuantity() != null ? d.getQuantity() : 0;
                    long prc = d.getPrice()    != null ? d.getPrice()    : 0;
                    s.put("quantity", (int)  s.get("quantity") + qty);
                    s.put("revenue",  (long) s.get("revenue")  + prc * qty);
                }
            }
        }

        List<Map<String, Object>> sorted = new ArrayList<>(mangaStats.values());
        sorted.sort((a, b) -> Integer.compare((int) b.get("quantity"), (int) a.get("quantity")));
        List<Map<String, Object>> top10  = sorted.stream().limit(10).collect(Collectors.toList());

        List<Manga> badSelling = allMangas.stream()
                .filter(m -> !mangaStats.containsKey(m.getId())
                        && m.getStockQuantity() != null && m.getStockQuantity() > 0)
                .collect(Collectors.toList());

        Map<Long, LocalDate> lastSaleDates = buildLastSaleMap(allOrders);

        // ── Section 1: Top 10 ────────────────────────────────────────────────
        int rowIdx = 5;
        rowIdx = createSectionLabel(sheet, wb, rowIdx, numCols,
                "1.  TOP 10 TRUYỆN BÁN CHẠY NHẤT TRONG KỲ", COLOR_ACCENT, COLOR_WHITE);
        createTableHeader(sheet, wb, rowIdx++, colTop);

        CellStyle dataStyle = buildDataStyle(wb, false);
        CellStyle dataAlt   = buildDataStyle(wb, true);
        CellStyle numStyle  = buildNumStyle(wb, false);
        CellStyle numAlt    = buildNumStyle(wb, true);

        if (top10.isEmpty()) {
            emptyRow(sheet, wb, rowIdx++, "Không có dữ liệu bán hàng trong kỳ.", colTop.length);
        } else {
            int stt = 1;
            for (Map<String, Object> stat : top10) {
                boolean alt = (stt % 2 == 0);
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(18);
                Manga m = (Manga) stat.get("manga");
                fillCells(row, colTop.length, alt ? dataAlt : dataStyle);
                row.getCell(0).setCellValue(stt++);
                row.getCell(1).setCellValue(str(m.getId()));
                row.getCell(2).setCellValue(str(m.getTitle()));
                row.getCell(3).setCellValue(m.getCategory() != null ? m.getCategory().getCategoryName() : "");
                row.getCell(4).setCellValue(m.getPublisher() != null ? m.getPublisher().getPublisherName() : "");
                row.getCell(5).setCellStyle(alt ? numAlt : numStyle);
                row.getCell(5).setCellValue((int) stat.get("quantity"));
                row.getCell(6).setCellStyle(alt ? numAlt : numStyle);
                row.getCell(6).setCellValue((long) stat.get("revenue"));
            }
        }

        rowIdx += 2; // Khoảng cách giữa 2 bảng

        // ── Section 2: Bad Selling ───────────────────────────────────────────
        XSSFColor badColor = fromHex("8B1A1A");
        rowIdx = createSectionLabel(sheet, wb, rowIdx, numCols,
                "2.  DANH SÁCH TRUYỆN BÁN Ế (KHÔNG CÓ LƯỢT BÁN TRONG KỲ)", badColor, COLOR_WHITE);
        createTableHeader(sheet, wb, rowIdx++, colBad);

        DateTimeFormatter dfDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (badSelling.isEmpty()) {
            emptyRow(sheet, wb, rowIdx++, "Toàn bộ truyện đều có lượt bán trong kỳ.", colBad.length);
        } else {
            CellStyle warnRed = buildWarningCellStyle(wb, COLOR_WARN_RED_BG, IndexedColors.RED);
            int stt = 1;
            for (Manga m : badSelling) {
                boolean alt = (stt % 2 == 0);
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(18);
                fillCells(row, colBad.length, alt ? dataAlt : dataStyle);

                LocalDate lastSale = lastSaleDates.get(m.getId());
                String lastSaleStr = lastSale != null ? lastSale.format(dfDate) : "Chưa từng bán";
                String note;
                CellStyle noteStyle;
                if (lastSale != null && lastSale.isBefore(LocalDate.now().minusMonths(3))) {
                    note = "Dead stock (> 3 tháng)";
                    noteStyle = warnRed;
                } else {
                    note = "Ế trong kỳ";
                    noteStyle = alt ? dataAlt : dataStyle;
                }

                row.getCell(0).setCellValue(stt++);
                row.getCell(1).setCellValue(str(m.getId()));
                row.getCell(2).setCellValue(str(m.getTitle()));
                row.getCell(3).setCellValue(m.getCategory() != null ? m.getCategory().getCategoryName() : "");
                row.getCell(4).setCellValue(m.getPublisher() != null ? m.getPublisher().getPublisherName() : "");
                row.getCell(5).setCellValue(m.getStockQuantity() != null ? m.getStockQuantity() : 0);
                row.getCell(6).setCellValue(lastSaleStr);
                row.getCell(7).setCellValue(note);
                row.getCell(7).setCellStyle(noteStyle);
            }
        }

        autoSize(sheet, numCols);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // STYLE BUILDERS
    // ═════════════════════════════════════════════════════════════════════════

    /** Dòng 0: tên công ty — navy-light bg, navy text, bold */
    private CellStyle buildCompanyNameStyle(XSSFWorkbook wb, int numCols) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(COLOR_NAVY_LIGHT);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        applyThinBorderAll(s);
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 11);
        f.setColor(COLOR_NAVY);
        s.setFont(f);
        return s;
    }

    /** Dòng 1: tiêu đề báo cáo — navy bg, white text, bold 16 */
    private CellStyle buildTitleStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(COLOR_NAVY);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        applyThinBorderAll(s);
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 16);
        f.setColor(COLOR_WHITE);
        s.setFont(f);
        return s;
    }

    /** Dòng 2-4: meta info — light bg, italic, center */
    private CellStyle buildMetaStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(COLOR_NAVY_LIGHT);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        applyThinBorderAll(s);
        XSSFFont f = wb.createFont();
        f.setItalic(true);
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        return s;
    }

    /** Header bảng (hàng cột) — navy bg, white bold, center */
    private void createTableHeader(XSSFSheet sheet, XSSFWorkbook wb, int rowIdx, String[] cols) {
        Row row = sheet.createRow(rowIdx);
        row.setHeightInPoints(22);
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(COLOR_NAVY);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        applyThinBorderAll(s);
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 11);
        f.setColor(COLOR_WHITE);
        s.setFont(f);
        for (int i = 0; i < cols.length; i++) {
            Cell c = row.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(s);
        }
    }

    /** Data cell — white or alt zebra background, bordered */
    private CellStyle buildDataStyle(XSSFWorkbook wb, boolean alt) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(alt ? COLOR_ROW_ALT : COLOR_WHITE);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        applyThinBorderAll(s);
        return s;
    }

    /** Number cell — same background, #,##0 format */
    private CellStyle buildNumStyle(XSSFWorkbook wb, boolean alt) {
        XSSFCellStyle s = (XSSFCellStyle) buildDataStyle(wb, alt);
        DataFormat fmt = wb.createDataFormat();
        s.setDataFormat(fmt.getFormat("#,##0"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    /** Wrap style for "Chi tiết sản phẩm" — bordered + wrap + top-left */
    private CellStyle buildWrapStyle(XSSFWorkbook wb, boolean alt) {
        XSSFCellStyle s = (XSSFCellStyle) buildDataStyle(wb, alt);
        s.setWrapText(true);
        s.setVerticalAlignment(VerticalAlignment.TOP);
        s.setAlignment(HorizontalAlignment.LEFT);
        return s;
    }

    /** KPI box — light navy bg, bold font */
    private CellStyle buildKpiStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(COLOR_KPI_BG);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        applyThinBorderAll(s);
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 11);
        s.setFont(f);
        return s;
    }

    /** Warning cell: colored background + colored bold text */
    private CellStyle buildWarningCellStyle(XSSFWorkbook wb, XSSFColor bgColor, IndexedColors fontColor) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(bgColor);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        applyThinBorderAll(s);
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setColor(fontColor.getIndex());
        s.setFont(f);
        return s;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    private void applyThinBorderAll(XSSFCellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    /** Tạo footer in (tên sheet + trang số) và các thiết lập in ấn */
    private void applyPrintSetup(XSSFSheet sheet, int numCols) {
        // Footer: tên sheet bên trái, trang số bên phải
        sheet.getFooter().setLeft("&F — &A");
        sheet.getFooter().setRight("Trang &P / &N");
        sheet.getHeader().setRight(COMPANY_NAME);

        // Fit to page width
        PrintSetup ps = sheet.getPrintSetup();
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);
        sheet.setFitToPage(true);
        sheet.setPrintGridlines(false);
        sheet.setDisplayGridlines(true);
    }

    private Map<Long, LocalDate> buildLastSaleMap(List<Order> orders) {
        Map<Long, LocalDate> map = new HashMap<>();
        for (Order o : orders) {
            if (!"DELIVERED".equals(o.getStatus()) && !"COMPLETED".equals(o.getStatus())) continue;
            if (o.getOrderDetails() == null) continue;
            for (OrderDetail d : o.getOrderDetails()) {
                if (d.getManga() == null || o.getCreatedAt() == null) continue;
                LocalDate date = o.getCreatedAt().toLocalDate();
                map.merge(d.getManga().getId(), date,
                        (existing, newDate) -> newDate.isAfter(existing) ? newDate : existing);
            }
        }
        return map;
    }

    /** Tạo dòng section label với nền màu accent */
    private int createSectionLabel(XSSFSheet sheet, XSSFWorkbook wb,
                                   int rowIdx, int numCols,
                                   String text, XSSFColor bgColor, XSSFColor fontColor) {
        Row row = sheet.createRow(rowIdx);
        row.setHeightInPoints(22);
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(bgColor);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        applyThinBorderAll(s);
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 12);
        f.setColor(fontColor);
        s.setFont(f);
        Cell c = row.createCell(0);
        c.setCellValue(text);
        c.setCellStyle(s);
        merge(sheet, rowIdx, rowIdx, 0, numCols - 1);
        return rowIdx + 1;
    }

    private void createKpiRow(XSSFSheet sheet, XSSFWorkbook wb,
                              int rowIdx, int numCols,
                              String text, CellStyle style, boolean large) {
        Row row = sheet.createRow(rowIdx);
        row.setHeightInPoints(large ? 24 : 20);
        Cell c = row.createCell(0);
        c.setCellValue(text);
        c.setCellStyle(style);
        merge(sheet, rowIdx, rowIdx, 0, numCols - 1);
    }

    private void emptyRow(XSSFSheet sheet, XSSFWorkbook wb, int rowIdx, String msg, int numCols) {
        Row row = sheet.createRow(rowIdx);
        row.setHeightInPoints(18);
        Cell c = row.createCell(0);
        c.setCellValue(msg);
        merge(sheet, rowIdx, rowIdx, 0, numCols - 1);
    }

    /** Tạo tất cả cell trong row với style mặc định */
    private void fillCells(Row row, int count, CellStyle style) {
        for (int i = 0; i < count; i++) {
            row.createCell(i).setCellStyle(style);
        }
    }

    private void setMeta(Row row, int col, String text, CellStyle style,
                         XSSFSheet sheet, int numCols) {
        Cell c = row.createCell(col);
        c.setCellValue(text);
        c.setCellStyle(style);
        merge(sheet, row.getRowNum(), row.getRowNum(), 0, numCols - 1);
    }

    private void merge(XSSFSheet sheet, int r1, int r2, int c1, int c2) {
        sheet.addMergedRegion(new CellRangeAddress(r1, r2, c1, c2));
    }

    private void autoSize(XSSFSheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
            // Thêm padding nhỏ sau autoSize
            int w = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(w + 512, 15000));
        }
    }

    private String str(Object o) {
        return o != null ? o.toString() : "";
    }

    private List<Order> filterOrdersByDate(List<Order> orders, LocalDate startDate, LocalDate endDate) {
        return orders.stream()
                .filter(o -> {
                    if (o.getCreatedAt() == null) return false;
                    LocalDate d = o.getCreatedAt().toLocalDate();
                    boolean ok = (startDate == null || !d.isBefore(startDate));
                    return ok && (endDate == null || !d.isAfter(endDate));
                })
                .collect(Collectors.toList());
    }

    private String getStatusVn(String status) {
        switch (status) {
            case "PENDING":   return "⏳ Đang xử lý";
            case "CONFIRMED": return "✅ Đã xác nhận";
            case "SHIPPING":  return "🚚 Đang giao hàng";
            case "DELIVERED": return "📦 Đã giao thành công";
            case "COMPLETED": return "✔️ Hoàn thành";
            case "CANCELLED": return "❌ Đã hủy";
            default:          return status;
        }
    }
}