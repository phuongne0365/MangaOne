package com.mangaone.controller;

import com.mangaone.entity.Publisher;
import com.mangaone.repository.PublisherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdminPublisherController {

    @Autowired
    private PublisherRepository publisherRepository;

    // helper: lấy danh sách publishers sắp xếp theo publisherId giảm dần
    private List<Publisher> findAllPublishersSorted() {
        return publisherRepository.findAll(Sort.by(Sort.Direction.DESC, "publisherId"));
    }

    // =========================
    // LIST
    // =========================
    @GetMapping("/admin/publishers")
    public String list(Model model,
                       @RequestParam(value = "q", required = false) String q) {

        List<Publisher> publishers = findAllPublishersSorted();

        if (q != null && !q.trim().isEmpty()) {
            String kw = q.trim().toLowerCase();
            publishers = publishers.stream()
                    .filter(p -> p.getPublisherName() != null && p.getPublisherName().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }

        model.addAttribute("publishers", publishers);
        model.addAttribute("publisher", new Publisher());
        model.addAttribute("q", q);
        model.addAttribute("currentPage", "publishers");
        return "admin/publisher-list";
    }

    // =========================
    // SAVE (ADD + UPDATE)
    // =========================
    @PostMapping("/admin/publishers/save")
    public String save(@ModelAttribute Publisher publisher, Model model) {

        // VALIDATION
        if (publisher.getPublisherName() == null ||
                publisher.getPublisherName().trim().isEmpty()) {
            return "redirect:/admin/publishers";
        }

        // CHECK DUPLICATE NAME
        for (Publisher p : findAllPublishersSorted()) {

            // Thêm mới
            if (publisher.getPublisherId() == null) {
                if (p.getPublisherName().trim()
                        .equalsIgnoreCase(publisher.getPublisherName().trim())) {

                    model.addAttribute("error", "Tên nhà xuất bản đã tồn tại!");
                    model.addAttribute("publishers", findAllPublishersSorted());
                    model.addAttribute("publisher", publisher);

                    return "admin/publisher-list";
                }
            }

            // Update
            else {
                if (!p.getPublisherId().equals(publisher.getPublisherId()) &&
                        p.getPublisherName().trim()
                                .equalsIgnoreCase(publisher.getPublisherName().trim())) {

                    model.addAttribute("error", "Tên nhà xuất bản đã tồn tại!");
                    model.addAttribute("publishers", findAllPublishersSorted());
                    model.addAttribute("publisher", publisher);

                    return "admin/publisher-list";
                }
            }
        }

        publisherRepository.save(publisher);
        return "redirect:/admin/publishers";
    }

    // =========================
    // EDIT
    // =========================
    @GetMapping("/admin/publishers/edit/{id}")
    public String edit(@PathVariable Integer id,
                       Model model,
                       @RequestParam(value = "q", required = false) String q) {

        Publisher publisher = publisherRepository.findById(id).orElse(null);

        List<Publisher> publishers = findAllPublishersSorted();
        if (q != null && !q.trim().isEmpty()) {
            String kw = q.trim().toLowerCase();
            publishers = publishers.stream()
                    .filter(p -> p.getPublisherName() != null && p.getPublisherName().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }

        model.addAttribute("publisher", publisher);
        model.addAttribute("publishers", publishers);
        model.addAttribute("q", q);
        model.addAttribute("currentPage", "publishers");

        return "admin/publisher-list";
    }

    // =========================
    // DELETE
    // =========================
    @GetMapping("/admin/publishers/delete/{id}")
    public String delete(@PathVariable Integer id, Model model) {

        Publisher publisher = publisherRepository.findById(id).orElse(null);

        // CHECK PUBLISHER HAS MANGAS
        if (publisher != null &&
                publisher.getMangas() != null &&
                !publisher.getMangas().isEmpty()) {

            model.addAttribute("error", "Không thể xóa nhà xuất bản đang chứa truyện!");
            model.addAttribute("publishers", findAllPublishersSorted());
            model.addAttribute("publisher", new Publisher());

            return "admin/publisher-list";
        }

        publisherRepository.deleteById(id);
        return "redirect:/admin/publishers";
    }
}