package com.mangaone.service;

import com.mangaone.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    /**
     * Lấy toàn bộ danh sách thể loại để hiển thị lên menu
     */
    List<Category> getAllCategories();

    /**
     *Lấy chi tiết một thể loại theo ID (dùng khi Admin sửa).
     */
    Optional<Category> getCategoryById(Integer id);

    /**
     *Thêm mới hoặc cập nhật thể loại.
     */
    Category saveCategory(Category category);

    /**
     * Xóa thể loại (có kiểm tra ràng buộc: không xóa nếu còn truyện).
     */
    void deleteCategory(Integer id);
}
