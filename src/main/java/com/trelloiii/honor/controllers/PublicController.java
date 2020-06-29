package com.trelloiii.honor.controllers;

import com.trelloiii.honor.model.Post;
import com.trelloiii.honor.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    //TODO upload posts

}
