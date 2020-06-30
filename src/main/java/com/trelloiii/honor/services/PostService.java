package com.trelloiii.honor.services;

import com.trelloiii.honor.exceptions.EntityNotFoundException;
import com.trelloiii.honor.model.Post;
import com.trelloiii.honor.model.PostType;
import com.trelloiii.honor.repository.PostRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    Logger logger=LoggerFactory.getLogger(PostService.class);
    @Value("${upload.path}")
    private String uploadPath;
    private final PostRepository postRepository;
    private final UploadService uploadService;

    public PostService(PostRepository postRepository, UploadService uploadService) {
        this.postRepository = postRepository;
        this.uploadService = uploadService;
    }

    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }

    public Post findById(Long id) {
        return postRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Post with id = %d not found", id))
        );
    }

    public Post updatePost(String title,
                           String description,
                           String shortDescription,
                           MultipartFile titleImage,
                           MultipartFile titleImageMini,
                           MultipartFile[] postImages,
                           String type,
                           Long id) throws IOException {
        logger.info("Update post with id={}, description={}, shortDescription={}, type={}, images count={}",id,description,shortDescription,type,postImages.length);
        Post post = findById(id);

        UrlHelper helper = getPaths(post.getId());

        Optional.ofNullable(title).ifPresent(post::setTitle);
        Optional.ofNullable(shortDescription).ifPresent(post::setShortDescription);
        Optional.ofNullable(type).ifPresent(t -> post.setType(PostType.valueOf(t)));
        Optional.ofNullable(titleImage).ifPresent(image -> {
            try {
                uploadService.uploadImage(image, helper.getPathToUpload(), helper.getURL());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Optional.ofNullable(titleImageMini).ifPresent(image -> {
            try {
                uploadService.uploadImage(image, helper.getPathToUpload(), helper.getURL());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Optional.ofNullable(description).ifPresent(d -> {
            try {
                post.setDescription(processDescription(d, postImages, helper.getPathToUpload(), helper.getURL()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return postRepository.save(post);
    }

    public Post uploadPost(String title,
                           String description,
                           String shortDescription,
                           MultipartFile titleImage,
                           MultipartFile titleImageMini,
                           MultipartFile[] postImages,
                           String type) throws IOException {
        logger.info("Upload post with description={}, shortDescription={}, type={}, images count={}",description,shortDescription,type,postImages.length);

        PostType postType = PostType.valueOf(type);

        Post post = new Post();
        post.setShortDescription(shortDescription);
        post.setTime(LocalDateTime.now());
        post.setTitle(title);
        post.setType(postType);
        post = postRepository.save(post);

        uploadService.createFolders(uploadPath, post.getId());

        UrlHelper helper = getPaths(post.getId());
        String pathToUpload = helper.getPathToUpload();
        String URL = helper.getURL();

        post.setTitleImage(uploadService.uploadImage(titleImage, pathToUpload, URL));
        post.setTitleImageMini(uploadService.uploadImage(titleImageMini, pathToUpload, URL));

        post.setDescription(processDescription(description, postImages, pathToUpload, URL));
        return postRepository.save(post);
    }

    private String processDescription(String description,
                                      MultipartFile[] postImages,
                                      String pathToUpload,
                                      String URL) throws IOException {
        if (postImages.length > 0) {
            String[] buf = description.split("_paste_");
            StringBuilder textBuilder = new StringBuilder();
            int i = 0;
            for (String s : buf) {
                if (i < postImages.length) {
                    String imagePath = uploadService.uploadImage(postImages[i], pathToUpload, URL);
                    textBuilder
                            .append(s)
                            .append("<img src=\"")
                            .append(imagePath)
                            .append("\">");
                }else{
                    textBuilder.append(s);
                }
                i++;
            }
            return textBuilder.toString();
        }
        return description;
    }

    private UrlHelper getPaths(Long id) {
        String URL = String.join("/", "posts", String.valueOf(id)); // as example posts/12
        String pathToUpload = String.join("/", uploadPath, URL); // as example /home/uploads/posts/12
        return new UrlHelper(URL, pathToUpload);
    }

    @Data
    @AllArgsConstructor
    static class UrlHelper {
        private String URL;
        private String pathToUpload;
    }

}
