package com.trelloiii.honor.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.trelloiii.honor.view.Views;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@JsonView(Views.ImportantView.class)
public class GalleryAlbum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDateTime time;
    @OneToMany(mappedBy = "album",cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonView(Views.FullView.class)
    private List<GalleryImage> images;

    public GalleryAlbum(String name) {
        this.name=name;
        this.time=LocalDateTime.now();
    }
}
