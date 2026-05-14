package com.mangaone.service.impl;

import com.mangaone.entity.Publisher;
import com.mangaone.repository.PublisherRepository;
import com.mangaone.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PublisherServiceImpl implements PublisherService {

    @Autowired
    private PublisherRepository publisherRepository;

    @Override
    public List<Publisher> getAllPublishers() {
        return publisherRepository.findAll();
    }

    @Override
    public Optional<Publisher> getPublisherById(Integer id) {
        return publisherRepository.findById(id);
    }

    @Override
    public Publisher savePublisher(Publisher publisher) {
        return publisherRepository.save(publisher);
    }

    @Override
    public void deletePublisher(Integer id) {
        publisherRepository.deleteById(id);
    }

    @Override
    public Publisher updatePublisher(Integer id, Publisher publisher) {
        Optional<Publisher> existing = publisherRepository.findById(id);
        if (existing.isPresent()) {
            Publisher p = existing.get();
            p.setPublisherName(publisher.getPublisherName());
            return publisherRepository.save(p);
        }
        return null;
    }
}