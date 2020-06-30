package com.trelloiii.honor.controllers;

import com.trelloiii.honor.model.Post;
import com.trelloiii.honor.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/public")
public class PublicController {
    private final PostService postService;

    public PublicController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public String helloWorld(){
        return "Hello world";
    }

    @GetMapping("/post")
    public List<Post> getPosts(){
        return postService.findAllPosts();
    }
    @GetMapping("/post/{id}")
    public Post getPostById(@PathVariable Long id){
        return postService.findById(id);
    }
    @PostMapping("/post")
    public ResponseEntity<?> uploadPost(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam("short") String shortDescription,
            @RequestParam MultipartFile titleImage,
            @RequestParam("mini") MultipartFile titleImageMini,
            @RequestParam MultipartFile[] postImages,
            @RequestParam String type,
            HttpServletResponse response
    ){
        try {
            return ResponseEntity.ok(postService.uploadPost(title, description, shortDescription, titleImage, titleImageMini, postImages, type));
        } catch (IOException e) {
            response.setStatus(400);
            return ResponseEntity.of(Optional.empty());
        }
    }
    @PutMapping("/post/{id}")
    public ResponseEntity<?> updatePost(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(value = "short",required = false) String shortDescription,
            @RequestParam(required = false) MultipartFile titleImage,
            @RequestParam(value = "mini",required = false) MultipartFile titleImageMini,
            @RequestParam(required = false) MultipartFile[] postImages,
            @RequestParam(required = false) String type,
            @PathVariable Long id,
            HttpServletResponse response
    ){
        try{
            return ResponseEntity.ok(postService.updatePost(title,description,shortDescription,titleImage,titleImageMini,postImages,type,id));
        }catch (IOException e){
            e.printStackTrace();
            response.setStatus(400);
            return ResponseEntity.of(Optional.empty());
        }
    }

}
