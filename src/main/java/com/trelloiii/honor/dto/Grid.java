package com.trelloiii.honor.dto;

import com.trelloiii.honor.model.PostType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Grid {
    private String image;
    private String title;
    private String type;
    private String url;
    private Long id;
}
