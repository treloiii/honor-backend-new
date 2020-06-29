package com.trelloiii.honor.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
public class Veterans {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fio;
    private String post;
    private String rank;
    @ManyToMany(
       cascade = {CascadeType.ALL}
    )
    @JoinTable(
            name="ordens_to_people",
            joinColumns = {@JoinColumn(name="veteran_id")},
            inverseJoinColumns = {@JoinColumn(name = "orden_id")}
    )
    private List<Ordens> ordens;
}
