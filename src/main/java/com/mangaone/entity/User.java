package com.mangaone.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "USERS")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    private Long userId;
    
    @Column(name = "email", unique = true, length = 100)
    private String email;
    private String password;	
    private String fullName;
    private String phoneNumber;
    private String address;
    private String role = "USER"; // Mặc định ai đăng ký cũng là USER thường
    @Column(name = "is_active")
    private Boolean isActive = true;
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public Boolean getIsActive() {
		return isActive;
	}
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
	
	
    // TODO: Bạn tự Generate Getters và Setters ở đây nhé (Chuột phải -> Source -> Generate Getters and Setters)
}