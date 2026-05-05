package com.mangaone.controller;

import com.mangaone.entity.Manga;
import com.mangaone.repository.MangaRepository;
import com.mangaone.service.CategoryService;
import com.mangaone.service.MangaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MangaController {

    @Autowired
    private MangaRepository mangaRepository;

    @Autowired
    private MangaService mangaService;

    @Autowired
    private CategoryService categoryService; // Dùng để hiển thị menu thể loại trên navbar

    /**
     * UC01 — Trang Kho Truyện: lấy toàn bộ danh sách từ DB, đẩy vào Model.
     *
     * [GIẢI THÍCH QUERY ĐỂ BÁO CÁO]
     * - mangaRepository.findAll() là một phương thức có sẵn do JpaRepository cung cấp.
     * - Spring Data JPA sẽ tự động dịch lệnh này thành câu SQL:
     * SELECT * FROM MANGAS;
     * - Kết quả trả về là một List<Manga>, mỗi phần tử là một hàng dữ liệu
     * trong bảng MANGAS được ánh xạ thành đối tượng Java tương ứng.
     * - model.addAttribute("mangas", ...) đặt danh sách vào Model với key "mangas"
     * để Thymeleaf có thể truy cập bằng cú pháp th:each="manga : ${mangas}".
     */
    @GetMapping("/mangas")
    public String khoTruyen(@RequestParam(value = "category_id", required = false) Long categoryId, Model model) {
        List<Manga> mangas;
        
        // Kiểm tra xem khách có gửi kèm category_id (bấm vào từng Thể loại) không?
        if (categoryId != null) {
            // Có thì nhờ kho lấy truyện theo đúng thể loại đó
            mangas = mangaRepository.findByCategory_CategoryId(categoryId);
        } else {
            // Không có (tức là bấm nút "Tất cả") thì lấy hết
            mangas = mangaRepository.findAll();
        }
        
        model.addAttribute("mangas", mangas);
        model.addAttribute("categories", categoryService.getAllCategories()); // Thêm categories
        return "kho-truyen";
    }

    @GetMapping("/kho-truyen") // Khi gõ /kho-truyen trên trình duyệt thì hàm này chạy
    public String listMangas(Model model) {
        // Lấy danh sách truyện và đặt tên là "listMangas" để gửi ra giao diện
        model.addAttribute("listMangas", mangaService.getAllMangas());
        // Thêm danh sách categories để hiển thị menu trên header
        model.addAttribute("categories", categoryService.getAllCategories());
        return "manga-list"; // Sẽ tìm file manga-list.html trong thư mục templates
    }

    // ================= MỞ TRANG CHI TIẾT TRUYỆN =================
    @GetMapping("/manga/{id}")
    public String showMangaDetail(@PathVariable("id") Long id, Model model) {
        Manga manga = mangaService.getMangaById(id); // Lấy thông tin chi tiết truyện

        if (manga != null) {
            model.addAttribute("manga", manga);
            // Giữ lại menu thể loại trên thanh điều hướng
            model.addAttribute("categories", categoryService.getAllCategories());
            return "manga-detail"; // Trỏ tới file manga-detail.html
        }

        return "redirect:/kho-truyen"; // Nếu không tìm thấy truyện, quay lại kho truyện
    }
}