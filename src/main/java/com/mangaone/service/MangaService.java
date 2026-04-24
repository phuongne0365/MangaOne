package com.mangaone.service;

import com.mangaone.entity.Manga;
import java.util.List;

public interface MangaService {
    List<Manga> getAllMangas(); // Liệt kê tất cả truyện
    Manga getMangaById(Integer id); // Xem chi tiết 1 cuốn
    void saveManga(Manga manga); // Lưu hoặc cập nhật truyện
    void deleteManga(Integer id); // Xóa truyện
}