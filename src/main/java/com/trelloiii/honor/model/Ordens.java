package com.trelloiii.honor.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class Ordens {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String shortDescription;
    private String titleImage;
    @ManyToMany(mappedBy = "ordens")
    private List<Veterans> veterans;
}
