package com.trelloiii.honor.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
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
    private Post post;
}
