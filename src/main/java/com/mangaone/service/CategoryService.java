package com.mangaone.service;

import com.mangaone.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    /**
     * UC02, UC11 — Lấy toàn bộ danh sách thể loại để hiển thị lên menu
     * điều hướng (navbar) của giao diện web (Thymeleaf/Model).
     */
    List<Category> getAllCategories();

    /**
     * UC11 — Lấy chi tiết một thể loại theo ID (dùng khi Admin sửa).
     */
    Optional<Category> getCategoryById(Integer id);

    /**
     * UC11 — Thêm mới hoặc cập nhật thể loại.
     */
    Category saveCategory(Category category);

    /**
     * UC14a — Xóa thể loại (có kiểm tra ràng buộc: không xóa nếu còn truyện).
     * @throws IllegalStateException nếu thể loại đang có truyện liên kết.
     */
    void deleteCategory(Integer id);
}
