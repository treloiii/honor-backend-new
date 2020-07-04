package com.trelloiii.honor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.trelloiii.honor.view.Views;
import lombok.Data;

import javax.persistence.*;
import javax.swing.text.View;
import java.util.List;

@Data
@Entity
@Table(name="honor_veterans")
@JsonView(Views.FullView.class)
public class Veterans {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fio;
    @Column(length = 1024)
    private String post;
    @Column(name = "rankk")
    private String rank;
    @ManyToOne
    @JoinColumn(name = "orden_id")
    @JsonIgnore
    private Ordens ordens;
}
