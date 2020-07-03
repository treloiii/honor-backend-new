package com.trelloiii.honor.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.trelloiii.honor.dto.PageContentDto;
import com.trelloiii.honor.exceptions.BadPostTypeException;
import com.trelloiii.honor.exceptions.EntityNotFoundException;
import com.trelloiii.honor.model.Comments;
import com.trelloiii.honor.model.Post;
import com.trelloiii.honor.model.User;
import com.trelloiii.honor.services.PostService;
import com.trelloiii.honor.view.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/post")
@CrossOrigin
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
                                         @RequestParam(required = false) Integer itemsPerPage,
                                         @RequestParam String type){
        return postService.findAllPosts(page,itemsPerPage,type);
    }
    @GetMapping("/{id}")
    @JsonView(Views.FullView.class)
    public ResponseEntity<Post> getPostById(@PathVariable Long id, @AuthenticationPrincipal User user){
        try {
            boolean sendRawComments = Optional.ofNullable(user).isPresent(); // if request from admin, we need to send all comments includes inactive
            return ResponseEntity.ok(sendRawComments?postService.findByIdRawComments(id):postService.findById(id));
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> uploadPost(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam("short") String shortDescription,
            @RequestParam MultipartFile titleImage,
            @RequestParam("mini") MultipartFile titleImageMini,
            @RequestParam MultipartFile[] postImages,
            @RequestParam String type
    ){
        try {
            return ResponseEntity.ok(postService.uploadPost(title, description, shortDescription, titleImage, titleImageMini, postImages, type));
        } catch (IOException | BadPostTypeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updatePost( //TODO сделать изменение времени
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(value = "short",required = false) String shortDescription,
            @RequestParam(required = false) MultipartFile titleImage,
            @RequestParam(value = "mini",required = false) MultipartFile titleImageMini,
            @RequestParam(required = false) MultipartFile[] postImages,
            @RequestParam(required = false) String type,
            @PathVariable Long id
    ){
        try{
            return ResponseEntity.ok(postService.updatePost(title,description,shortDescription,titleImage,titleImageMini,postImages,type,id));
        }catch (EntityNotFoundException e){
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deletePost(@PathVariable Long id){
        postService.deletePost(id);
    }

    @PostMapping("/comments")
    public ResponseEntity<Comments> addCommentToPost(@RequestParam Long id,
                                     @RequestParam String nickname,
                                     @RequestParam String text){
        try {
            return ResponseEntity.ok(postService.addComment(id, nickname, text));
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/comments")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void changeActive(@RequestParam Boolean active,
                             @RequestParam Long id,
                             @RequestParam Long postId){
        postService.setActiveComments(active,id,postId);
    }
    @DeleteMapping("/comments/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteComment(@PathVariable Long id,@RequestParam Long postId){
        postService.deleteComments(id,postId);
    }

}
