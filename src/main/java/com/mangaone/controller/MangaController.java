package com.mangaone.controller;

import com.mangaone.entity.Manga;
import com.mangaone.repository.MangaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MangaController {

    @Autowired
    private MangaRepository mangaRepository;

    /**
     * UC01 — Trang Kho Truyện: lấy toàn bộ danh sách từ DB, đẩy vào Model.
     *
     * [GIẢI THÍCH QUERY ĐỂ BÁO CÁO]
     * - mangaRepository.findAll() là một phương thức có sẵn do JpaRepository cung cấp.
     * - Spring Data JPA sẽ tự động dịch lệnh này thành câu SQL:
     *       SELECT * FROM MANGAS;
     * - Kết quả trả về là một List<Manga>, mỗi phần tử là một hàng dữ liệu
     *   trong bảng MANGAS được ánh xạ thành đối tượng Java tương ứng.
     * - model.addAttribute("mangas", ...) đặt danh sách vào Model với key "mangas"
     *   để Thymeleaf có thể truy cập bằng cú pháp th:each="manga : ${mangas}".
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
        return "kho-truyen";
    }
}