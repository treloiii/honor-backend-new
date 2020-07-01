package com.trelloiii.honor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.trelloiii.honor.view.Views;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@ToString(of = {"id"})
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(Views.ImportantView.class)
    private Long id;
    @JsonView(Views.ImportantView.class)
    private String title;
    @JsonView(Views.FullView.class)
    private String description;
    @JsonView(Views.FullView.class)
    private String titleImage;
    @JsonView(Views.ImportantView.class)
    private LocalDateTime time;
    @JsonView(Views.ImportantView.class)
    private String titleImageMini;
    @JsonView(Views.ImportantView.class)
    private String shortDescription;
    @JsonIgnore
    private PostType type;
    @OneToMany(mappedBy = "post")
    @JsonView(Views.FullView.class)
    //TODO pagination of comments
    private List<Comments> comments;
}
