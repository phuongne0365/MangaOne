package com.mangaone.service.impl;

import com.mangaone.entity.Manga;
import com.mangaone.entity.Order;
import com.mangaone.entity.OrderDetail;
import com.mangaone.repository.MangaRepository;
import com.mangaone.repository.OrderRepository;
import com.mangaone.service.ExcelReportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelReportServiceImpl implements ExcelReportService {

    private final OrderRepository orderRepository;
    private final MangaRepository mangaRepository;

    public ExcelReportServiceImpl(OrderRepository orderRepository, MangaRepository mangaRepository) {
        this.orderRepository = orderRepository;
        this.mangaRepository = mangaRepository;
    }

    @Override
    public byte[] generateReport(LocalDate startDate, LocalDate endDate, String reporterName) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            List<Order> allOrders = orderRepository.findAll();
            List<Manga> allMangas = mangaRepository.findAll();

            List<Order> filteredOrders = filterOrdersByDate(allOrders, startDate, endDate);

            createSheet1(workbook, filteredOrders, startDate, endDate, reporterName);
            createSheet2(workbook, allMangas, allOrders, reporterName);
            createSheet3(workbook, filteredOrders, startDate, endDate, reporterName);
            createSheet4(workbook, allMangas, filteredOrders, allOrders, startDate, endDate, reporterName);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xuất file Excel báo cáo", e);
        }
    }

    private List<Order> filterOrdersByDate(List<Order> orders, LocalDate startDate, LocalDate endDate) {
        return orders.stream()
                .filter(o -> {
                    if (o.getCreatedAt() == null) return false;
                    LocalDate orderDate = o.getCreatedAt().toLocalDate();
                    boolean afterStart = (startDate == null) || !orderDate.isBefore(startDate);
                    boolean beforeEnd = (endDate == null) || !orderDate.isAfter(endDate);
                    return afterStart && beforeEnd;
                })
                .collect(Collectors.toList());
    }

    private void createHeaderInfo(Sheet sheet, Workbook workbook, String title, int numColumns, LocalDate startDate, LocalDate endDate, String reporterName) {
        // Dòng 1: Tên báo cáo
        Row row1 = sheet.createRow(0);
        Cell cell1 = row1.createCell(0);
        cell1.setCellValue(title.toUpperCase());
        CellStyle styleTitle = workbook.createCellStyle();
        Font fontTitle = workbook.createFont();
        fontTitle.setBold(true);
        fontTitle.setFontHeightInPoints((short) 16);
        styleTitle.setFont(fontTitle);
        styleTitle.setAlignment(HorizontalAlignment.CENTER);
        cell1.setCellStyle(styleTitle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, numColumns - 1));

        // Dòng 2: Kỳ báo cáo
        Row row2 = sheet.createRow(1);
        Cell cell2 = row2.createCell(0);
        String periodStr = "Kỳ báo cáo: Toàn thời gian";
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (startDate != null && endDate != null) {
            periodStr = "Kỳ báo cáo: Từ ngày " + startDate.format(df) + " đến ngày " + endDate.format(df);
        } else if (startDate != null) {
            periodStr = "Kỳ báo cáo: Từ ngày " + startDate.format(df);
        } else if (endDate != null) {
            periodStr = "Kỳ báo cáo: Đến ngày " + endDate.format(df);
        }
        cell2.setCellValue(periodStr);

        CellStyle styleItalicCenter = workbook.createCellStyle();
        Font fontItalic = workbook.createFont();
        fontItalic.setItalic(true);
        styleItalicCenter.setFont(fontItalic);
        styleItalicCenter.setAlignment(HorizontalAlignment.CENTER);
        cell2.setCellStyle(styleItalicCenter);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, numColumns - 1));

        // Dòng 3: Người lập báo cáo
        Row row3 = sheet.createRow(2);
        Cell cell3 = row3.createCell(0);
        cell3.setCellValue("Người lập báo cáo: " + reporterName);
        cell3.setCellStyle(styleItalicCenter);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, numColumns - 1));

        // Dòng 4: Thời gian xuất báo cáo
        Row row4 = sheet.createRow(3);
        Cell cell4 = row4.createCell(0);
        cell4.setCellValue("Thời gian xuất báo cáo: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        cell4.setCellStyle(styleItalicCenter);
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, numColumns - 1));
    }

    private void createTableHeader(Sheet sheet, Workbook workbook, int rowIndex, String[] columns) {
        Row row = sheet.createRow(rowIndex);
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private CellStyle createBorderedStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createSheet1(Workbook workbook, List<Order> filteredOrders, LocalDate startDate, LocalDate endDate, String reporterName) {
        String[] columns = {"STT", "Thời gian", "Mã Truyện", "Tên truyện", "Danh mục", "Số lượng bán", "Doanh thu mang lại"};
        Sheet sheet = workbook.createSheet("TỔNG QUAN & DOANH THU");
        
        createHeaderInfo(sheet, workbook, "BÁO CÁO TỔNG QUAN & DOANH THU", columns.length, startDate, endDate, reporterName);

        long totalRevenue = 0;
        int countSuccess = 0;
        int countProcessing = 0;
        int countCanceled = 0;

        Map<Long, Map<String, Object>> mangaStats = new HashMap<>();

        for (Order o : filteredOrders) {
            boolean isSuccess = "DELIVERED".equals(o.getStatus()) || "COMPLETED".equals(o.getStatus());
            boolean isProcessing = "PENDING".equals(o.getStatus()) || "CONFIRMED".equals(o.getStatus()) || "SHIPPING".equals(o.getStatus());
            boolean isCanceled = "CANCELLED".equals(o.getStatus());

            if (isSuccess) {
                countSuccess++;
                if (o.getTotalAmount() != null) totalRevenue += o.getTotalAmount();
            } else if (isProcessing) {
                countProcessing++;
            } else if (isCanceled) {
                countCanceled++;
            }

            if (isSuccess && o.getOrderDetails() != null) {
                for (OrderDetail detail : o.getOrderDetails()) {
                    Manga m = detail.getManga();
                    if (m == null) continue;
                    Map<String, Object> stats = mangaStats.computeIfAbsent(m.getId(), k -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("manga", m);
                        map.put("quantity", 0);
                        map.put("revenue", 0L);
                        return map;
                    });
                    int qty = detail.getQuantity() != null ? detail.getQuantity() : 0;
                    long prc = detail.getPrice() != null ? detail.getPrice() : 0;
                    stats.put("quantity", (int) stats.get("quantity") + qty);
                    stats.put("revenue", (long) stats.get("revenue") + prc * qty);
                }
            }
        }

        CellStyle kpiStyle = workbook.createCellStyle();
        Font kpiFont = workbook.createFont();
        kpiFont.setBold(true);
        kpiStyle.setFont(kpiFont);

        Row rowKpi1 = sheet.createRow(5);
        Cell cellKpi1 = rowKpi1.createCell(0);
        cellKpi1.setCellValue("Tổng doanh thu (Chỉ tính đơn 'Đã giao thành công'): " + String.format("%,d", totalRevenue) + " VNĐ");
        cellKpi1.setCellStyle(kpiStyle);

        Row rowKpi2 = sheet.createRow(6);
        Cell cellKpi2 = rowKpi2.createCell(0);
        cellKpi2.setCellValue("Tổng số đơn hàng: " + (countSuccess + countProcessing + countCanceled));
        cellKpi2.setCellStyle(kpiStyle);
        
        Row rowKpi3 = sheet.createRow(7);
        Cell cellKpi3 = rowKpi3.createCell(0);
        cellKpi3.setCellValue("Chi tiết: Thành công: " + countSuccess + " | Đang xử lý: " + countProcessing + " | Đã hủy: " + countCanceled);
        
        int tableRowIdx = 9;
        createTableHeader(sheet, workbook, tableRowIdx, columns);

        List<Map<String, Object>> sortedStats = new ArrayList<>(mangaStats.values());
        sortedStats.sort((a, b) -> Integer.compare((int) b.get("quantity"), (int) a.get("quantity")));

        CellStyle dataStyle = createBorderedStyle(workbook);
        
        String timeStr = "Toàn thời gian";
        if (startDate != null && endDate != null) {
            timeStr = startDate.format(DateTimeFormatter.ofPattern("dd/MM")) + " - " + endDate.format(DateTimeFormatter.ofPattern("dd/MM"));
        } else if (startDate != null) {
            timeStr = "Từ " + startDate.format(DateTimeFormatter.ofPattern("dd/MM"));
        } else if (endDate != null) {
            timeStr = "Đến " + endDate.format(DateTimeFormatter.ofPattern("dd/MM"));
        }

        int rowIdx = tableRowIdx + 1;
        int stt = 1;
        for (Map<String, Object> stat : sortedStats) {
            Row row = sheet.createRow(rowIdx++);
            Manga m = (Manga) stat.get("manga");
            
            Cell[] cells = new Cell[columns.length];
            for(int i = 0; i < columns.length; i++) {
                cells[i] = row.createCell(i);
                cells[i].setCellStyle(dataStyle);
            }

            cells[0].setCellValue(stt++);
            cells[1].setCellValue(timeStr);
            cells[2].setCellValue(m.getId() != null ? m.getId().toString() : "");
            cells[3].setCellValue(m.getTitle() != null ? m.getTitle() : "");
            cells[4].setCellValue(m.getCategory() != null ? m.getCategory().getCategoryName() : "");
            cells[5].setCellValue((int) stat.get("quantity"));
            cells[6].setCellValue((long) stat.get("revenue"));
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSheet2(Workbook workbook, List<Manga> allMangas, List<Order> allOrders, String reporterName) {
        String[] columns = {"STT", "ID Truyện", "Tên truyện", "Nhà xuất bản", "Thể loại", "Giá bán", "Tồn kho vật lý", "Mức độ cảnh báo"};
        Sheet sheet = workbook.createSheet("TỒN KHO & VẬN HÀNH");
        
        createHeaderInfo(sheet, workbook, "BÁO CÁO TỒN KHO & VẬN HÀNH", columns.length, null, null, reporterName);

        int tableRowIdx = 5;
        createTableHeader(sheet, workbook, tableRowIdx, columns);

        Map<Long, LocalDate> lastSaleDates = new HashMap<>();
        for (Order o : allOrders) {
            if ("DELIVERED".equals(o.getStatus()) || "COMPLETED".equals(o.getStatus())) {
                if (o.getOrderDetails() != null) {
                    for (OrderDetail detail : o.getOrderDetails()) {
                        if (detail.getManga() != null && o.getCreatedAt() != null) {
                            LocalDate orderDate = o.getCreatedAt().toLocalDate();
                            LocalDate existing = lastSaleDates.get(detail.getManga().getId());
                            if (existing == null || orderDate.isAfter(existing)) {
                                lastSaleDates.put(detail.getManga().getId(), orderDate);
                            }
                        }
                    }
                }
            }
        }

        CellStyle dataStyle = createBorderedStyle(workbook);
        
        CellStyle redStyle = workbook.createCellStyle();
        redStyle.cloneStyleFrom(dataStyle);
        Font redFont = workbook.createFont();
        redFont.setColor(IndexedColors.RED.getIndex());
        redFont.setBold(true);
        redStyle.setFont(redFont);
        
        CellStyle orangeStyle = workbook.createCellStyle();
        orangeStyle.cloneStyleFrom(dataStyle);
        Font orangeFont = workbook.createFont();
        orangeFont.setColor(IndexedColors.DARK_YELLOW.getIndex()); 
        orangeFont.setBold(true);
        orangeStyle.setFont(orangeFont);

        int rowIdx = tableRowIdx + 1;
        int stt = 1;
        for (Manga m : allMangas) {
            Row row = sheet.createRow(rowIdx++);
            
            Cell[] cells = new Cell[columns.length];
            for(int i = 0; i < columns.length; i++) {
                cells[i] = row.createCell(i);
                cells[i].setCellStyle(dataStyle);
            }

            int stock = m.getStockQuantity() != null ? m.getStockQuantity() : 0;
            String warning = "Bình thường";
            CellStyle warningStyle = dataStyle;

            if (stock == 0) {
                warning = "Hết sạch";
                warningStyle = redStyle;
            } else {
                LocalDate lastSale = lastSaleDates.get(m.getId());
                if (lastSale != null && lastSale.isBefore(LocalDate.now().minusMonths(3))) {
                    warning = "Dead stock (Lưu kho > 3 tháng)";
                    warningStyle = redStyle;
                } else if (stock < 10) {
                    warning = "Sắp hết hàng (<10)";
                    warningStyle = orangeStyle;
                }
            }

            cells[0].setCellValue(stt++);
            cells[1].setCellValue(m.getId() != null ? m.getId().toString() : "");
            cells[2].setCellValue(m.getTitle() != null ? m.getTitle() : "");
            cells[3].setCellValue(m.getPublisher() != null ? m.getPublisher().getPublisherName() : "");
            cells[4].setCellValue(m.getCategory() != null ? m.getCategory().getCategoryName() : "");
            cells[5].setCellValue(m.getPrice() != null ? m.getPrice() : 0.0);
            cells[6].setCellValue(stock);
            
            cells[7].setCellValue(warning);
            cells[7].setCellStyle(warningStyle);
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSheet3(Workbook workbook, List<Order> filteredOrders, LocalDate startDate, LocalDate endDate, String reporterName) {
        String[] columns = {"STT", "Mã Đơn Hàng", "Ngày đặt", "Tên người nhận", "Số điện thoại", "Địa chỉ giao hàng chi tiết", "Chi tiết sản phẩm", "Trạng thái đơn", "Tổng thanh toán"};
        Sheet sheet = workbook.createSheet("VẬN HÀNH ĐƠN HÀNG");
        
        createHeaderInfo(sheet, workbook, "BÁO CÁO VẬN HÀNH ĐƠN HÀNG", columns.length, startDate, endDate, reporterName);

        int tableRowIdx = 5; // Dòng 6 bắt đầu là Header của bảng
        createTableHeader(sheet, workbook, tableRowIdx, columns);

        CellStyle dataStyle = createBorderedStyle(workbook);
        
        CellStyle wrapStyle = workbook.createCellStyle();
        wrapStyle.cloneStyleFrom(dataStyle);
        wrapStyle.setWrapText(true);

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        int rowIdx = tableRowIdx + 1;
        int stt = 1;
        for (Order o : filteredOrders) {
            Row row = sheet.createRow(rowIdx++);
            
            Cell[] cells = new Cell[columns.length];
            for(int i = 0; i < columns.length; i++) {
                cells[i] = row.createCell(i);
                cells[i].setCellStyle(dataStyle);
            }

            String orderDateStr = o.getCreatedAt() != null ? o.getCreatedAt().format(df) : "";
            
            String productDetails = "";
            if (o.getOrderDetails() != null) {
                productDetails = o.getOrderDetails().stream()
                        .filter(d -> d.getManga() != null)
                        .map(d -> "- " + d.getManga().getTitle() + " x" + d.getQuantity())
                        .collect(Collectors.joining("\n"));
            }

            cells[0].setCellValue(stt++);
            cells[1].setCellValue(o.getOrderId() != null ? o.getOrderId().toString() : "");
            cells[2].setCellValue(orderDateStr);
            cells[3].setCellValue(o.getReceiverName() != null ? o.getReceiverName() : "");
            cells[4].setCellValue(o.getReceiverPhone() != null ? o.getReceiverPhone() : "");
            cells[5].setCellValue(o.getShippingAddress() != null ? o.getShippingAddress() : "");
            cells[6].setCellValue(productDetails);
            cells[6].setCellStyle(wrapStyle);
            cells[7].setCellValue(o.getStatus() != null ? getStatusVn(o.getStatus()) : "");
            cells[8].setCellValue(o.getTotalAmount() != null ? o.getTotalAmount() : 0);
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSheet4(Workbook workbook, List<Manga> allMangas, List<Order> filteredOrders, List<Order> allOrders, LocalDate startDate, LocalDate endDate, String reporterName) {
        Sheet sheet = workbook.createSheet("BÁN CHẠY & BÁN Ế");
        String[] columnsTop = {"STT", "Mã Truyện", "Tên truyện", "Danh mục", "Số lượng bán", "Doanh thu"};
        String[] columnsBad = {"STT", "Mã Truyện", "Tên truyện", "Danh mục", "Tồn kho", "Ngày bán gần nhất", "Tình trạng"};

        createHeaderInfo(sheet, workbook, "THỐNG KÊ TRUYỆN BÁN CHẠY & BÁN Ế", 7, startDate, endDate, reporterName);

        Map<Long, Map<String, Object>> mangaStats = new HashMap<>();
        for (Order o : filteredOrders) {
            boolean isSuccess = "DELIVERED".equals(o.getStatus()) || "COMPLETED".equals(o.getStatus());
            if (isSuccess && o.getOrderDetails() != null) {
                for (OrderDetail detail : o.getOrderDetails()) {
                    Manga m = detail.getManga();
                    if (m == null) continue;
                    Map<String, Object> stats = mangaStats.computeIfAbsent(m.getId(), k -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("manga", m);
                        map.put("quantity", 0);
                        map.put("revenue", 0L);
                        return map;
                    });
                    int qty = detail.getQuantity() != null ? detail.getQuantity() : 0;
                    long prc = detail.getPrice() != null ? detail.getPrice() : 0;
                    stats.put("quantity", (int) stats.get("quantity") + qty);
                    stats.put("revenue", (long) stats.get("revenue") + prc * qty);
                }
            }
        }

        List<Map<String, Object>> sortedStats = new ArrayList<>(mangaStats.values());
        sortedStats.sort((a, b) -> Integer.compare((int) b.get("quantity"), (int) a.get("quantity")));

        List<Map<String, Object>> topSelling = sortedStats.stream().limit(10).collect(Collectors.toList());

        List<Manga> badSelling = new ArrayList<>();
        for (Manga m : allMangas) {
            if (!mangaStats.containsKey(m.getId()) && (m.getStockQuantity() != null && m.getStockQuantity() > 0)) {
                badSelling.add(m);
            }
        }

        Map<Long, LocalDate> lastSaleDates = new HashMap<>();
        for (Order o : allOrders) {
            if ("DELIVERED".equals(o.getStatus()) || "COMPLETED".equals(o.getStatus())) {
                if (o.getOrderDetails() != null) {
                    for (OrderDetail detail : o.getOrderDetails()) {
                        if (detail.getManga() != null && o.getCreatedAt() != null) {
                            LocalDate orderDate = o.getCreatedAt().toLocalDate();
                            LocalDate existing = lastSaleDates.get(detail.getManga().getId());
                            if (existing == null || orderDate.isAfter(existing)) {
                                lastSaleDates.put(detail.getManga().getId(), orderDate);
                            }
                        }
                    }
                }
            }
        }

        int rowIdx = 5;

        Row topLabelRow = sheet.createRow(rowIdx++);
        Cell topLabelCell = topLabelRow.createCell(0);
        topLabelCell.setCellValue("1. TOP 10 TRUYỆN BÁN CHẠY NHẤT TRONG KỲ");
        CellStyle labelStyle = workbook.createCellStyle();
        Font labelFont = workbook.createFont();
        labelFont.setBold(true);
        labelFont.setColor(IndexedColors.GREEN.getIndex());
        labelFont.setFontHeightInPoints((short) 12);
        labelStyle.setFont(labelFont);
        topLabelCell.setCellStyle(labelStyle);

        createTableHeader(sheet, workbook, rowIdx++, columnsTop);
        CellStyle dataStyle = createBorderedStyle(workbook);

        int stt = 1;
        if (topSelling.isEmpty()) {
            Row row = sheet.createRow(rowIdx++);
            Cell cell = row.createCell(0);
            cell.setCellValue("Không có dữ liệu bán hàng trong kỳ.");
            sheet.addMergedRegion(new CellRangeAddress(rowIdx-1, rowIdx-1, 0, columnsTop.length - 1));
        } else {
            for (Map<String, Object> stat : topSelling) {
                Row row = sheet.createRow(rowIdx++);
                Manga m = (Manga) stat.get("manga");
                for (int i = 0; i < columnsTop.length; i++) {
                    row.createCell(i).setCellStyle(dataStyle);
                }
                row.getCell(0).setCellValue(stt++);
                row.getCell(1).setCellValue(m.getId() != null ? m.getId().toString() : "");
                row.getCell(2).setCellValue(m.getTitle() != null ? m.getTitle() : "");
                row.getCell(3).setCellValue(m.getCategory() != null ? m.getCategory().getCategoryName() : "");
                row.getCell(4).setCellValue((int) stat.get("quantity"));
                row.getCell(5).setCellValue((long) stat.get("revenue"));
            }
        }

        rowIdx += 2;

        Row badLabelRow = sheet.createRow(rowIdx++);
        Cell badLabelCell = badLabelRow.createCell(0);
        badLabelCell.setCellValue("2. DANH SÁCH TRUYỆN BÁN Ế (KHÔNG CÓ LƯỢT BÁN TRONG KỲ)");
        CellStyle badLabelStyle = workbook.createCellStyle();
        Font badLabelFont = workbook.createFont();
        badLabelFont.setBold(true);
        badLabelFont.setColor(IndexedColors.RED.getIndex());
        badLabelFont.setFontHeightInPoints((short) 12);
        badLabelStyle.setFont(badLabelFont);
        badLabelCell.setCellStyle(badLabelStyle);

        createTableHeader(sheet, workbook, rowIdx++, columnsBad);

        stt = 1;
        DateTimeFormatter dfDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (badSelling.isEmpty()) {
            Row row = sheet.createRow(rowIdx++);
            Cell cell = row.createCell(0);
            cell.setCellValue("Toàn bộ truyện đều có lượt bán trong kỳ.");
            sheet.addMergedRegion(new CellRangeAddress(rowIdx-1, rowIdx-1, 0, columnsBad.length - 1));
        } else {
            for (Manga m : badSelling) {
                Row row = sheet.createRow(rowIdx++);
                for (int i = 0; i < columnsBad.length; i++) {
                    row.createCell(i).setCellStyle(dataStyle);
                }
                LocalDate lastSale = lastSaleDates.get(m.getId());
                String lastSaleStr = (lastSale != null) ? lastSale.format(dfDate) : "Chưa từng bán";
                String note = "Ế trong kỳ";
                if (lastSale != null && lastSale.isBefore(LocalDate.now().minusMonths(3))) {
                    note = "Dead stock (>3 tháng)";
                }

                row.getCell(0).setCellValue(stt++);
                row.getCell(1).setCellValue(m.getId() != null ? m.getId().toString() : "");
                row.getCell(2).setCellValue(m.getTitle() != null ? m.getTitle() : "");
                row.getCell(3).setCellValue(m.getCategory() != null ? m.getCategory().getCategoryName() : "");
                row.getCell(4).setCellValue(m.getStockQuantity() != null ? m.getStockQuantity() : 0);
                row.getCell(5).setCellValue(lastSaleStr);
                row.getCell(6).setCellValue(note);
            }
        }

        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String getStatusVn(String status) {
        switch (status) {
            case "PENDING": return "Đang xử lý";
            case "CONFIRMED": return "Đã xác nhận";
            case "SHIPPING": return "Đang giao hàng";
            case "DELIVERED": return "Đã giao thành công";
            case "COMPLETED": return "Hoàn thành";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }
}
