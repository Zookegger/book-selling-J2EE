package com.group.book_selling.services;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.group.book_selling.models.Publisher;
import com.group.book_selling.repositories.IPublisherRepository;
import com.group.book_selling.utils.SlugUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublisherService {

    private final IPublisherRepository publisherRepository;

    @Transactional(readOnly = true)
    public List<Publisher> findAll() {
        return publisherRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Transactional(readOnly = true)
    public Publisher findById(Long id) {
        return publisherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nha xuat ban"));
    }

    public Publisher create(Publisher request) {
        request.setId(null);
        request.setSlug(SlugUtils.slugify(request.getName()));
        return publisherRepository.save(request);
    }

    public Publisher update(Long id, Publisher request) {
        Publisher existing = findById(id);

        existing.setName(request.getName());
        existing.setSlug(SlugUtils.slugify(request.getName()));
        existing.setDescription(request.getDescription());
        existing.setLocation(request.getLocation());
        existing.setContactEmail(request.getContactEmail());
        existing.setWebsite(request.getWebsite());
        existing.setLogo(request.getLogo());
        existing.setActive(request.isActive());

        return publisherRepository.save(existing);
    }

    public void delete(Long id) {
        if (!publisherRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nha xuat ban");
        }
        publisherRepository.deleteById(id);
    }
}
