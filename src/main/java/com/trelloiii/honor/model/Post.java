package com.trelloiii.honor.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String titleImage;
    private LocalDateTime time;
    private String titleImageMini;
    private String shortDescription;
    private PostType type;
    @OneToMany(mappedBy = "post")
    private List<Comments> comments;
}
