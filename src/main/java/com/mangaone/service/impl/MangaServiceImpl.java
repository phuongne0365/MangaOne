package com.mangaone.service.impl;

import com.mangaone.entity.Manga;
import com.mangaone.repository.MangaRepository;
import com.mangaone.service.MangaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service 
public class MangaServiceImpl implements MangaService {

    @Autowired
    private MangaRepository mangaRepository; 

    @Override
    public List<Manga> getAllMangas() {
        // Trả về toàn bộ danh sách truyện từ bảng Mangas
        return mangaRepository.findAll();
    }

    @Override
    public Manga getMangaById(Integer id) {
        // Tìm truyện theo ID, nếu không thấy thì trả về null
        return mangaRepository.findById(id).orElse(null);
    }

    @Override
    public void saveManga(Manga manga) {
        // Lưu truyện mới hoặc cập nhật nếu đã tồn tại ID
        mangaRepository.save(manga);
    }

    @Override
    public void deleteManga(Integer id) {
        // Xóa truyện dựa trên ID
        mangaRepository.deleteById(id);
    }
}