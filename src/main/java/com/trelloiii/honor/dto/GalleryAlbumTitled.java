package com.trelloiii.honor.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.trelloiii.honor.model.GalleryAlbum;
import com.trelloiii.honor.view.Views;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonView(Views.ImportantView.class)
public class GalleryAlbumTitled {
    private String titleImage;
    private GalleryAlbum album;
}
