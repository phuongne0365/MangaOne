package com.mangaone.service.impl;

import com.mangaone.entity.Category;
import com.mangaone.repository.CategoryRepository;
import com.mangaone.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> getCategoryById(Integer id) {
        return categoryRepository.findById(id);
    }

    @Override
    @Transactional
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * Xóa thể loại:
     * Không cho xóa nếu thể loại đang có truyện liên kết.
     */
    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thể loại với ID: " + id));

        // Kiểm tra ràng buộc: nếu còn truyện thì từ chối xóa 
        if (category.getMangas() != null && !category.getMangas().isEmpty()) {
            throw new IllegalStateException(
                "Không thể xóa thể loại \"" + category.getCategoryName()
                + "\" vì hiện đang có " + category.getMangas().size() + " truyện thuộc thể loại này."
            );
        }

        categoryRepository.deleteById(id);
    }
}
