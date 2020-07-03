package com.trelloiii.honor.repository;

import com.trelloiii.honor.model.GalleryImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GalleryImageRepository extends JpaRepository<GalleryImage,Long> {
    GalleryImage findFirst1ByOrderByIdDesc();
}
