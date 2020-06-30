package com.trelloiii.honor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class GalleryImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String url;
    @ManyToOne
    @JoinColumn(name = "album_id")
    @JsonIgnore
    private GalleryAlbum album;
}
