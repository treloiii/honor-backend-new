package com.trelloiii.honor.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.trelloiii.honor.view.Views;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@JsonView(Views.ImportantView.class)
public class Ordens {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 512)
    private String name;
    @JsonView(Views.FullView.class)
    @Column(columnDefinition = "TEXT")
    @Lob
    private String description;
    @Column(length = 512)
    private String shortDescription;
    @Column(length = 512)
    private String titleImage;
    @OneToMany(mappedBy = "ordens")
    @JsonView(Views.FullView.class)
    //TODO pagination veterans
    private List<Veterans> veterans;
}
