package com.trelloiii.honor.services;

import com.trelloiii.honor.dto.PageContentDto;
import com.trelloiii.honor.dto.UrlHelper;
import com.trelloiii.honor.exceptions.EntityNotFoundException;
import com.trelloiii.honor.model.Ordens;
import com.trelloiii.honor.model.Veterans;
import com.trelloiii.honor.repository.OrdenRepository;
import com.trelloiii.honor.repository.VeteransRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrdenService {
    private final Logger logger = LoggerFactory.getLogger(PostService.class);
    private final OrdenRepository ordenRepository;
    private final VeteransRepository veteransRepository;
    private final UploadService uploadService;
    @Value("${content.page}")
    private int CONTENT_PER_PAGE;
    @Value("${upload.path}")
    private String uploadPath;

    public OrdenService(OrdenRepository ordenRepository, VeteransRepository veteransRepository, UploadService uploadService) {
        this.ordenRepository = ordenRepository;
        this.veteransRepository = veteransRepository;
        this.uploadService = uploadService;
    }
    @Cacheable("ordens")
    public PageContentDto<Ordens> getAllOrdens(Integer page, Integer itemsPerPage) {
        Integer perPage = Optional.ofNullable(itemsPerPage).orElse(CONTENT_PER_PAGE);
        PageRequest pageRequest = PageRequest.of(page, perPage, Sort.by(Sort.Direction.DESC,"id"));
        Page<Ordens> ordensPage = ordenRepository.findAll(pageRequest);
        return new PageContentDto<>(
                ordensPage.getContent(),
                pageRequest.getPageNumber(),
                ordensPage.getTotalPages()
        );
    }
    @Cacheable(value = "orden",key = "#id")
    public Ordens findById(Long id) {
        return ordenRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Orden with id = %d not found", id))
        );
    }

    @CacheEvict(value = "ordens",allEntries = true)
    public Ordens addOrden(String name, String description, String shortDescription, MultipartFile titleImage) throws IOException {
        logger.info("Add orden with name {}, description {}, shortDescription {}", name, description, shortDescription);
        Ordens orden = new Ordens();
        orden.setDescription(description);
        orden.setShortDescription(shortDescription);
        orden.setName(name);
        orden = ordenRepository.save(orden);

        uploadService.createOrdensFolders(uploadPath, orden.getId());

        UrlHelper helper = UrlHelper.getPaths(orden.getId(), uploadPath, "ordens");

        String titleUrl = uploadService.uploadImage(titleImage, helper.getPathToUpload(), helper.getURL());
        orden.setTitleImage(titleUrl);
        return ordenRepository.save(orden);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "ordens",allEntries = true),
            },
            put = {
                    @CachePut(value = "orden",key = "#id")
            }
    )
    public Ordens updateOrden(String name, String description, String shortDescription, MultipartFile titleImage, Long id) {
        logger.info("Update orden with id {}, set name {}, description {}, shortDescription {}", id, name, description, shortDescription);
        Ordens orden = findById(id);
        Optional.ofNullable(name).ifPresent(orden::setName);
        Optional.ofNullable(description).ifPresent(orden::setDescription);
        Optional.ofNullable(shortDescription).ifPresent(orden::setShortDescription);
        Optional.ofNullable(titleImage).ifPresent(image -> {
            UrlHelper helper = UrlHelper.getPaths(orden.getId(), uploadPath, "ordens");
            try {
                uploadService.removeOld(helper.getPathToUpload());
                String titleImageUrl = uploadService.uploadImage(image, helper.getPathToUpload(), helper.getURL());
                orden.setTitleImage(titleImageUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return ordenRepository.save(orden);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "ordens",allEntries = true),
                    @CacheEvict(value = "orden",key = "#id")
            }
    )
    public void deleteOrden(Long id) {
        logger.info("Delete orden with id {}", id);
        UrlHelper helper = UrlHelper.getPaths(id, uploadPath, "ordens");
        uploadService.removeAll(helper.getPathToUpload());
        ordenRepository.deleteById(id);
    }

    public Veterans addVeteran(Long id, String fio, String post, String rank) {
        Veterans veteran = new Veterans();
        veteran.setFio(fio);
        veteran.setPost(post);
        veteran.setRank(rank);
        veteran.setOrdens(findById(id));
        return veteransRepository.save(veteran);
    }
}
