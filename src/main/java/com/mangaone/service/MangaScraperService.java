package com.mangaone.service;

import com.mangaone.entity.Manga;
import com.mangaone.entity.Category;
import com.mangaone.entity.Publisher;
import com.mangaone.repository.MangaRepository;
import com.mangaone.repository.CategoryRepository;
import com.mangaone.repository.PublisherRepository;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MangaScraperService {

    @Autowired private MangaRepository mangaRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PublisherRepository publisherRepository;

    public int scrapePhuongNamWithLimit() {

        int LIMIT_MANGA = 500;
        int PAGE_SIZE   = 50;
        int totalCrawled = 0;
        int page = 1;

        // ── Chuẩn bị Category ───────────────────────────────────────────
        Category defaultCategory = null;
        for (Category cat : categoryRepository.findAll()) {
            if (cat.getCategoryName() != null &&
                    cat.getCategoryName().toLowerCase().contains("manga")) {
                defaultCategory = cat;
                break;
            }
        }
        if (defaultCategory == null) {
            Category cat = new Category();
            cat.setCategoryName("Manga (Phương Nam)");
            defaultCategory = categoryRepository.save(cat);
        }

        // ── Chuẩn bị Publisher ──────────────────────────────────────────
        Publisher defaultPublisher = null;
        for (Publisher pub : publisherRepository.findAll()) {
            if (pub.getPublisherName() != null &&
                    pub.getPublisherName().equalsIgnoreCase("NXB Phương Nam")) {
                defaultPublisher = pub;
                break;
            }
        }
        if (defaultPublisher == null) {
            Publisher pub = new Publisher();
            pub.setPublisherName("NXB Phương Nam");
            defaultPublisher = publisherRepository.save(pub);
        }

        // ── Load title đã có trong DB → chống lặp ───────────────────────
        Set<String> existingTitles = new HashSet<>();
        for (Manga m : mangaRepository.findAll()) {
            if (m.getTitle() != null) {
                existingTitles.add(m.getTitle().trim().toLowerCase());
            }
        }
        System.out.println(">>> DB hiện có " + existingTitles.size() + " truyện — sẽ bỏ qua trùng lặp.");

        // ── Vòng lặp qua từng trang ─────────────────────────────────────
        while (totalCrawled < LIMIT_MANGA) {

            String apiUrl = "https://nhasachphuongnam.com/collections/truyen-tranh-manga/products.json"
                    + "?limit=" + PAGE_SIZE + "&page=" + page;
            System.out.println(">>> Đang gọi API: " + apiUrl);

            try {
                String json = Jsoup.connect(apiUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Accept", "application/json")
                        .header("Accept-Language", "vi-VN,vi;q=0.9")
                        .ignoreContentType(true)
                        .timeout(20000)
                        .get()
                        .body()
                        .text();

                System.out.println("    → JSON length: " + json.length());

                // Đếm số sản phẩm bằng cách đếm "handle"
                int productCount = countOccurrences(json, "\"handle\":\"");
                System.out.println("    → Trang " + page + ": " + productCount + " sản phẩm");

                if (productCount == 0) {
                    System.out.println(">>> Hết dữ liệu. Dừng.");
                    break;
                }

                // Tách theo handle — mỗi product có đúng 1 handle
                String[] blocks = json.split("\"handle\":\"");
                List<Manga> batch = new ArrayList<>();

                for (int i = 1; i < blocks.length; i++) {
                    if (totalCrawled >= LIMIT_MANGA) break;

                    String block = blocks[i];

                    // Lấy title
                    String title = extractValue(block, "title");
                    if (title.isEmpty()) continue;

                    if (existingTitles.contains(title.toLowerCase())) {
                        System.out.println("    [SKIP] " + title);
                        continue;
                    }

                    // Lấy ảnh src đầu tiên
                    String imgUrl = extractValue(block, "src");

                    // Lấy giá
                    String priceStr = extractValue(block, "price").replaceAll("[^0-9.]", "");
                    double price = priceStr.isEmpty() ? 35000.0 : Double.parseDouble(priceStr);

                    // Lấy tác giả từ vendor
                    String vendor = extractValue(block, "vendor");
                    String author = vendor.isEmpty() ? "Chưa rõ tác giả" : vendor;

                    Manga manga = new Manga();
                    manga.setTitle(title);
                    manga.setAuthor(author);
                    manga.setDescription("Truyện tranh Manga chọn lọc, phát hành chính hãng tại Nhà sách Phương Nam.");
                    manga.setPrice(price);
                    manga.setStockQuantity(120);
                    manga.setImage(imgUrl);
                    manga.setCategory(defaultCategory);
                    manga.setPublisher(defaultPublisher);

                    batch.add(manga);
                    existingTitles.add(title.toLowerCase());
                    totalCrawled++;
                    System.out.println("    [OK] " + title + " — " + price + "d");
                }

                if (!batch.isEmpty()) {
                    mangaRepository.saveAll(batch);
                    System.out.println("    → Đã lưu " + batch.size() + " cuốn. Tổng: " + totalCrawled);
                }

                if (productCount < PAGE_SIZE) {
                    System.out.println(">>> Đã qua trang cuối.");
                    break;
                }

                page++;
                Thread.sleep(1000);

            } catch (IOException e) {
                System.err.println(">>> Lỗi kết nối trang " + page + ": " + e.getMessage());
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("========== KẾT THÚC ==========");
        System.out.println(">>> TỔNG TRUYỆN MỚI NẠP: " + totalCrawled + " CUỐN.");
        return totalCrawled;
    }

    private int countOccurrences(String text, String search) {
        int count = 0, idx = 0;
        while ((idx = text.indexOf(search, idx)) != -1) {
            count++;
            idx += search.length();
        }
        return count;
    }

    private String extractValue(String block, String key) {
        String search = "\"" + key + "\":\"";
        int start = block.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        int end = block.indexOf("\"", start);
        if (end == -1) return "";
        String raw = block.substring(start, end).replace("\\/", "/").trim();
        return decodeUnicode(raw);
    }
    private String decodeUnicode(String s) {
        if (s == null) return s;
        StringBuilder normalized = new StringBuilder();
        for (int j = 0; j < s.length(); j++) {
            if (s.charAt(j) == '\\' && j + 1 < s.length() && s.charAt(j + 1) == 'U') {
                normalized.append("\\u");
                j++; // bỏ qua 'U'
            } else {
                normalized.append(s.charAt(j));
            }
        }
        s = normalized.toString();

        if (!s.contains("\\u")) return s;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            if (s.charAt(i) == '\\'
                    && i + 5 <= s.length()
                    && s.charAt(i + 1) == 'u') {
                String hex = s.substring(i + 2, i + 6);
                boolean isHex = hex.chars().allMatch(c ->
                    (c >= '0' && c <= '9') ||
                    (c >= 'a' && c <= 'f') ||
                    (c >= 'A' && c <= 'F'));
                if (isHex) {
                    sb.append((char) Integer.parseInt(hex, 16));
                    i += 6;
                    continue;
                }
            }
            sb.append(s.charAt(i));
            i++;
        }
        return sb.toString();
    }
}