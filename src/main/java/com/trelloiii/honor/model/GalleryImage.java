package com.trelloiii.honor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.trelloiii.honor.view.Views;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@JsonView(Views.ImportantView.class)
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