package com.trelloiii.honor.services;

import com.trelloiii.honor.dto.PageContentDto;
import com.trelloiii.honor.dto.UrlHelper;
import com.trelloiii.honor.exceptions.EntityNotFoundException;
import com.trelloiii.honor.model.GalleryAlbum;
import com.trelloiii.honor.model.GalleryImage;
import com.trelloiii.honor.repository.GalleryAlbumRepository;
import com.trelloiii.honor.repository.GalleryImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
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
import java.util.Objects;
import java.util.Optional;

@Service
public class GalleryAlbumService {
    private final Logger logger = LoggerFactory.getLogger(GalleryAlbumService.class);
    @Value("${content.page}")
    private int CONTENT_PER_PAGE;
    @Value("${upload.path}")
    private String uploadPath;
    private final CacheManager cacheManager;
    private final UploadService uploadService;
    private final GalleryAlbumRepository galleryAlbumRepository;
    private final GalleryImageRepository galleryImageRepository;
    public GalleryAlbumService(CacheManager cacheManager, UploadService uploadService, GalleryAlbumRepository galleryAlbumRepository, GalleryImageRepository galleryImageRepository) {
        this.cacheManager = cacheManager;
        this.uploadService = uploadService;
        this.galleryAlbumRepository = galleryAlbumRepository;
        this.galleryImageRepository = galleryImageRepository;
    }

    @Cacheable("albums")
    public PageContentDto<GalleryAlbum> getAllAlbums(Integer page,Integer itemsPerPage) {
        Integer perPage= Optional.ofNullable(itemsPerPage).orElse(CONTENT_PER_PAGE);
        PageRequest pageRequest=PageRequest.of(page,perPage, Sort.by(Sort.Direction.DESC,"id"));
        Page<GalleryAlbum> albumPage =  galleryAlbumRepository.findAll(pageRequest);
        return new PageContentDto<>(
                albumPage.getContent(),
                pageRequest.getPageNumber(),
                albumPage.getTotalPages()
        );
    }
    @Cacheable(value = "album",key = "#id")
    public GalleryAlbum findById(Long id) {
        return galleryAlbumRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Entity Gallery album with id %d not found", id))
        );
    }

    @CacheEvict(value = "albums",allEntries = true)
    public GalleryAlbum addAlbum(String name) {
        logger.info("Add album with name {}",name);
        GalleryAlbum album = new GalleryAlbum(name);
        return galleryAlbumRepository.save(album);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "albums",allEntries = true),
                    @CacheEvict(value = "album",key = "#id")
            }
    )
    public void updateAlbum(String name, Long id) {
        logger.info("Update album with id {} to name {}",id,name);
        galleryAlbumRepository.changeAlbumName(name, id);
    }

    @Caching(
            evict={
                    @CacheEvict(value = "albums",allEntries = true),
                    @CacheEvict(value = "album",key = "#id")
            }
    )
    public void deleteAlbum(Long id) {
        logger.info("Delete album with id {}",id);
        UrlHelper urlHelper = UrlHelper.getPaths(id, uploadPath, "gallery");
        uploadService.removeAll(urlHelper.getPathToUpload());
        galleryAlbumRepository.deleteById(id);
    }

    @CacheEvict(value = "album",key = "#id")
    public GalleryImage addImage(Long id, String name, MultipartFile image) throws IOException {
        logger.info("Upload image with name {} to album with id {}",name,id);
        GalleryImage galleryImage = new GalleryImage();
        galleryImage.setAlbum(findById(id));
        galleryImage.setName(name);

        galleryImage.setUrl(uploadImage(id,image));
        return galleryImageRepository.save(galleryImage);
    }

    @CacheEvict(value = "album",key = "#id")
    public List<GalleryImage> addAllImages(Long id, MultipartFile[] images) throws IOException {
        logger.info("Upload batch images with size {} to album with id {}",images.length,id);
        GalleryAlbum album=findById(id);
        List<GalleryImage> galleryImages=new ArrayList<>();
        for (MultipartFile image : images) {
            GalleryImage galleryImage=new GalleryImage();
            galleryImage.setAlbum(album);
            galleryImage.setName(image.getOriginalFilename());
            galleryImage.setUrl(uploadImage(id,image));
            galleryImages.add(galleryImage);
        }
        return galleryImageRepository.saveAll(galleryImages);
    }

    private String uploadImage(Long id,MultipartFile image) throws IOException {
        uploadService.createGalleryFolders(uploadPath, id);
        UrlHelper urlHelper = UrlHelper.getPaths(id, uploadPath, "gallery");
        return uploadService.uploadImage(image, urlHelper.getPathToUpload(), urlHelper.getURL());
    }

    public void deleteImage(Long id){
        logger.info("Delete image with id {}",id);
        GalleryImage image=galleryImageRepository.findById(id).orElseThrow(()->
                new EntityNotFoundException(String.format("Entity GalleryImage with id %d not found",id))
        );
        UrlHelper helper=UrlHelper.getPaths(image.getAlbum().getId(),uploadPath,"gallery");
        try {
            String [] urlSplit=image.getUrl().split("/");
            String imageName=urlSplit[urlSplit.length-1];
            uploadService.removeFile(String.join("/",helper.getPathToUpload(),imageName));
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Objects.requireNonNull(cacheManager.getCache("album")).evictIfPresent(image.getAlbum().getId());
        galleryImageRepository.deleteById(id);
    }
}
