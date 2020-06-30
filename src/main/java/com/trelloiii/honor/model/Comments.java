package com.trelloiii.honor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.trelloiii.honor.view.Views;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@JsonView(Views.ImportantView.class)
public class Comments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nickname;
    private String text;
    private LocalDateTime time;
    private boolean active;
    @ManyToOne
    @JoinColumn(name="post_id")
    @JsonIgnore
    private Post post;
}
