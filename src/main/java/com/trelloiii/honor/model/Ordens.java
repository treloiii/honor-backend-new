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
    private String name;
    @JsonView(Views.FullView.class)
    private String description;
    private String shortDescription;
    private String titleImage;
    @OneToMany(mappedBy = "ordens")
    @JsonView(Views.FullView.class)
    private List<Veterans> veterans;
}
