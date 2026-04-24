package com.mangaone.repository;

import com.mangaone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Spring Boot sẽ TỰ ĐỘNG viết câu lệnh SQL: SELECT * FROM USERS WHERE email = ?
    User findByEmail(String email); 
    
}