package com.trelloiii.honor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UrlHelper {
    private String URL;
    private String pathToUpload;

    public static UrlHelper getPaths(Long id,String uploadPath,String type) {
        String URL = String.join("/", type, String.valueOf(id)); // as example posts/12 or ordens/12
        String pathToUpload = String.join("/", uploadPath, URL); // as example /home/uploads/posts/12 or /home/uploads/ordens/12
        return new UrlHelper(URL, pathToUpload);
    }
}
