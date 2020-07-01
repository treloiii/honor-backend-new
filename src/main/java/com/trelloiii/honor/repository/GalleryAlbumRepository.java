package com.trelloiii.honor.repository;

import com.trelloiii.honor.model.GalleryAlbum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface GalleryAlbumRepository extends JpaRepository<GalleryAlbum,Long> {
    @Transactional
    @Modifying
    @Query("update GalleryAlbum a set a.name=:name where a.id=:id")
    void changeAlbumName(String name,Long id);
    Page<GalleryAlbum> findAll(Pageable page);
}
