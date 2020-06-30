package com.trelloiii.honor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name="honor_veterans")
public class Veterans {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fio;
    private String post;
    @Column(name = "rankk")
    private String rank;
    @ManyToMany(
       cascade = {CascadeType.ALL}
    )
    @JoinTable(
            name="ordens_to_people",
            joinColumns = {@JoinColumn(name="veteran_id")},
            inverseJoinColumns = {@JoinColumn(name = "orden_id")}
    )
    @JsonIgnore
    private List<Ordens> ordens;
}
