package com.trelloiii.honor.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.trelloiii.honor.dto.PageContentDto;
import com.trelloiii.honor.model.Comments;
import com.trelloiii.honor.model.Post;
import com.trelloiii.honor.services.PostService;
import com.trelloiii.honor.view.Views;
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
@RequestMapping("/post")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/test")
    public String helloWorld(){
        return "Hello world";
    }

    @GetMapping
    @JsonView(Views.ImportantView.class)
    public PageContentDto<Post> getPosts(@RequestParam Integer page,
                                         @RequestParam(required = false) Integer itemsPerPage){
        return postService.findAllPosts(page,itemsPerPage);
    }
    @GetMapping("/{id}")
    @JsonView(Views.FullView.class)
    public Post getPostById(@PathVariable Long id){
        return postService.findById(id);
    }
    @PostMapping
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
    @PutMapping("/{id}")
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
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id){
        postService.deletePost(id);
    }

    @PostMapping("/comments")
    public Comments addCommentToPost(@RequestParam Long id,
                                     @RequestParam String nickname,
                                     @RequestParam String text){
        return postService.addComment(id,nickname,text);
    }
    @PutMapping("/comments")
    public void changeActive(@RequestParam Boolean active,
                                 @RequestParam Long id){
        postService.setActiveComments(active,id);
    }
    @DeleteMapping("/comments/{id}")
    public void deleteComment(@PathVariable Long id){
        postService.deleteComments(id);
    }

}
