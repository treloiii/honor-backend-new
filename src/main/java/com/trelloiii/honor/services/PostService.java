package com.trelloiii.honor.services;

import com.trelloiii.honor.exceptions.EntityNotFoundException;
import com.trelloiii.honor.model.Post;
import com.trelloiii.honor.repository.PostRepository;
import lombok.extern.log4j.Log4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    @Value("${upload.path}")
    private String uploadPath;
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
}
