package com.trelloiii.honor.services;

import com.trelloiii.honor.dto.PageContentDto;
import com.trelloiii.honor.dto.UrlHelper;
import com.trelloiii.honor.exceptions.EntityNotFoundException;
import com.trelloiii.honor.model.Comments;
import com.trelloiii.honor.model.Post;
import com.trelloiii.honor.model.PostType;
import com.trelloiii.honor.repository.CommentsRepository;
import com.trelloiii.honor.repository.PostRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {
    Logger logger=LoggerFactory.getLogger(PostService.class);
    @Value("${content.page}")
    private int CONTENT_PER_PAGE;
    @Value("${upload.path}")
    private String uploadPath;
    private final PostRepository postRepository;
    private final UploadService uploadService;
    private final CommentsRepository commentsRepository;
    public PostService(PostRepository postRepository, UploadService uploadService, CommentsRepository commentsRepository) {
        this.postRepository = postRepository;
        this.uploadService = uploadService;
        this.commentsRepository = commentsRepository;
    }
    public Post getLastByType(String type){
        return postRepository.getDistinctFirstByType(PostType.valueOf(type));
    }
    public PageContentDto<Post> findAllPosts(Integer page,Integer itemsPerPage) {
        Integer perPage=Optional.ofNullable(itemsPerPage).orElse(CONTENT_PER_PAGE);
        PageRequest pageRequest=PageRequest.of(page,perPage, Sort.by(Sort.Direction.DESC,"id"));
        Page<Post> postPage=postRepository.findAll(pageRequest);
        return new PageContentDto<>(
                postPage.getContent(),
                pageRequest.getPageNumber(),
                postPage.getTotalPages()
        );
    }

    public Post findById(Long id) {
        Post post= postRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Post with id = %d not found", id))
        );
        post.setComments(
                post.getComments()
                        .stream()
                        .filter(Comments::isActive)
                        .collect(Collectors.toList())
        );
        return post;
    }

    public void deletePost(Long id){
        logger.info("Delete post with id {}",id);
        UrlHelper helper = UrlHelper.getPaths(id,uploadPath,"posts");
        uploadService.removeAll(helper.getPathToUpload());
        postRepository.deleteById(id);
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

        UrlHelper helper = UrlHelper.getPaths(post.getId(),uploadPath,"posts");

        Optional.ofNullable(title).ifPresent(post::setTitle);
        Optional.ofNullable(shortDescription).ifPresent(post::setShortDescription);
        Optional.ofNullable(type).ifPresent(t -> post.setType(PostType.valueOf(t)));



        Optional.ofNullable(titleImage).ifPresent(image -> {
            try {
                String path=helper.getPathToUpload()+"/title";
                uploadService.removeOld(path);
                uploadService.uploadImage(image, path, helper.getURL()+"/title");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Optional.ofNullable(titleImageMini).ifPresent(image -> {
            try {
                String path=helper.getPathToUpload()+"/title_short";
                uploadService.removeOld(path);
                uploadService.uploadImage(image, path, helper.getURL()+"/title_short");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Optional.ofNullable(description).ifPresent(d -> {
            String path=helper.getPathToUpload()+"/description";
            try {
                uploadService.removeOld(path);
                post.setDescription(processDescription(d, postImages, path, helper.getURL()+"/description"));
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

        uploadService.createPostFolders(uploadPath, post.getId());

        UrlHelper helper = UrlHelper.getPaths(post.getId(),uploadPath,"posts");
        String pathToUpload = helper.getPathToUpload();
        String URL = helper.getURL();

        post.setTitleImage(uploadService.uploadImage(titleImage, pathToUpload+"/title", URL+"/title"));
        post.setTitleImageMini(uploadService.uploadImage(titleImageMini, pathToUpload+"/title_short", URL+"/title_short"));

        post.setDescription(processDescription(description, postImages, pathToUpload+"/description", URL+"/description"));
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



    public Comments addComment(Long id, String nickname, String text) {
        Comments comments=new Comments();
        comments.setActive(false);
        comments.setNickname(nickname);
        comments.setText(text);
        comments.setTime(LocalDateTime.now());
        comments.setPost(findById(id));
        return commentsRepository.save(comments);
    }

    public void setActiveComments(boolean active,Long id){
        commentsRepository.setActive(active,id);
    }
    public void deleteComments(Long id){
        commentsRepository.deleteById(id);
    }
    public Comments findCommentById(Long id){
        return commentsRepository.findById(id).orElseThrow(()->
            new EntityNotFoundException(String.format("Entity comments with id = %d not found",id))
        );
    }
}
