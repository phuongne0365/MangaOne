package com.mangaone.repository;

import com.mangaone.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // JpaRepository cung cấp sẵn: findAll(), findById(), save(), deleteById()...
    // Không cần khai báo thêm vì CategoryService chỉ cần lấy toàn bộ danh sách.
}
