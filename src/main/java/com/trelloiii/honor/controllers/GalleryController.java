package com.trelloiii.honor.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.trelloiii.honor.dto.GalleryAlbumTitled;
import com.trelloiii.honor.dto.PageContentDto;
import com.trelloiii.honor.model.GalleryAlbum;
import com.trelloiii.honor.model.GalleryImage;
import com.trelloiii.honor.services.GalleryAlbumService;
import com.trelloiii.honor.view.Views;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/gallery")
@CrossOrigin
public class GalleryController {
    private final GalleryAlbumService galleryAlbumService;

    public GalleryController(GalleryAlbumService galleryAlbumService) {
        this.galleryAlbumService = galleryAlbumService;
    }

    @GetMapping
    @JsonView(Views.ImportantView.class)
    public PageContentDto<GalleryAlbumTitled> getAllAlbums(@RequestParam Integer page,
                                                           @RequestParam(required = false) Integer itemsPerPage){
        return galleryAlbumService.getAllAlbums(page,itemsPerPage);
    }
    @GetMapping("/{id}")
    @JsonView(Views.FullView.class)
    public GalleryAlbum getAlbumById(@PathVariable Long id){
        return galleryAlbumService.findById(id);
    }
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public GalleryAlbum addAlbum(@RequestParam String name){
        return galleryAlbumService.addAlbum(name);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateAlbum(@RequestParam String name, @PathVariable Long id){
        galleryAlbumService.updateAlbum(name,id);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteAlbum(@PathVariable Long id){
        galleryAlbumService.deleteAlbum(id);
    }
    @PostMapping("/image")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> addImage(
            @RequestParam String name,
            @RequestParam MultipartFile image,
            @RequestParam Long id){
        try {
            return ResponseEntity.ok(galleryAlbumService.addImage(id, name, image));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    @PostMapping("/images")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> addImages(
            @RequestParam Long id,
            @RequestParam MultipartFile[] images
    ){
        try{
            return ResponseEntity.ok(galleryAlbumService.addAllImages(id,images));
        }catch (IOException e){
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    @DeleteMapping("/image/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteImage(@PathVariable Long id){
        galleryAlbumService.deleteImage(id);
    }


}
