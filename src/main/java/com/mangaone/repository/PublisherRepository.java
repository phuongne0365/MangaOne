package com.mangaone.repository;

import com.mangaone.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Integer> {
    // JpaRepository cung cấp đủ: findAll(), findById(), save(), deleteById()
}
