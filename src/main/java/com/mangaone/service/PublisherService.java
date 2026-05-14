package com.mangaone.service;

import com.mangaone.entity.Publisher;
import java.util.List;
import java.util.Optional;

public interface PublisherService {
    List<Publisher> getAllPublishers();
    Optional<Publisher> getPublisherById(Integer id);
    Publisher savePublisher(Publisher publisher);
    void deletePublisher(Integer id);
    Publisher updatePublisher(Integer id, Publisher publisher);
}