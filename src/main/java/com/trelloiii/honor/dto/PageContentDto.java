package com.trelloiii.honor.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.trelloiii.honor.view.Views;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonView(Views.ImportantView.class)
public class PageContentDto<T> {
    private List<T> content;
    private int pageNumber;
    private int totalPages;
}
