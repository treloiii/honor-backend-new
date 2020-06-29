package com.trelloiii.honor.services;

import com.trelloiii.honor.exceptions.EntityNotFoundException;
import com.trelloiii.honor.model.Post;
import com.trelloiii.honor.model.PostType;
import com.trelloiii.honor.repository.PostRepository;
import lombok.extern.log4j.Log4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {
    @Value("${upload.path}")
    private String uploadPath;
    @Value("${hostname}")
    private String hostname;
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }

    public Post findById(Long id) {
        return postRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Post with id = %d not found",id))
        );
    }
    public Post uploadPost(String title,
                           String description,
                           String shortDescription,
                           MultipartFile titleImage,
                           MultipartFile titleImageMini,
                           MultipartFile[] postImages,
                           String type) throws IOException {
        PostType postType=PostType.valueOf(type);

        Post post=new Post();
        post.setShortDescription(shortDescription);
        post.setTime(LocalDateTime.now());
        post.setTitle(title);
        post.setType(postType);
        post=postRepository.save(post);

        String pathToUpload=String.join("/",uploadPath,type,String.valueOf(post.getId()));
        String URL=String.join("/",hostname,pathToUpload);

        post.setTitleImage(uploadImage(titleImage,URL,pathToUpload));
        post.setTitleImageMini(uploadImage(titleImageMini,URL,pathToUpload));

        String[] buf = description.split("_paste_");
        StringBuilder textBuilder=new StringBuilder();
        int i=0;
        for (String s : buf) {
            if(i<postImages.length){
                String imagePath=uploadImage(postImages[i],URL,pathToUpload);
                textBuilder
                        .append(s)
                        .append("<img src=\"")
                        .append(imagePath)
                        .append("\">");
            }
        }
        post.setDescription(textBuilder.toString());
        return postRepository.save(post);
    }

    private String uploadImage(MultipartFile image,String URL,String pathToUpload){
        String imageName=image.getOriginalFilename();
        String contentType=image.getContentType();
        try {
            image.transferTo(new File(pathToUpload));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return URL + "/" + imageName + "." + contentType;
    }
}
