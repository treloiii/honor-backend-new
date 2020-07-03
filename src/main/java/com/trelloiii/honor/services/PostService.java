package com.trelloiii.honor.services;

import com.trelloiii.honor.dto.Grid;
import com.trelloiii.honor.dto.PageContentDto;
import com.trelloiii.honor.dto.UrlHelper;
import com.trelloiii.honor.exceptions.BadPostTypeException;
import com.trelloiii.honor.exceptions.EntityNotFoundException;
import com.trelloiii.honor.model.Comments;
import com.trelloiii.honor.model.GalleryImage;
import com.trelloiii.honor.model.Post;
import com.trelloiii.honor.model.PostType;
import com.trelloiii.honor.repository.CommentsRepository;
import com.trelloiii.honor.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.trelloiii.honor.model.PostType.*;

@Service
public class PostService {
    Logger logger = LoggerFactory.getLogger(PostService.class);
    @Value("${content.page}")
    private int CONTENT_PER_PAGE;
    @Value("${upload.path}")
    private String uploadPath;
    private final PostRepository postRepository;
    private final UploadService uploadService;
    private final CommentsRepository commentsRepository;
    private final GalleryAlbumService albumService;
    public PostService(PostRepository postRepository, UploadService uploadService, CommentsRepository commentsRepository, GalleryAlbumService albumService) {
        this.postRepository = postRepository;
        this.uploadService = uploadService;
        this.commentsRepository = commentsRepository;
        this.albumService = albumService;
    }

    public Post getLastByType(String type) {
        return postRepository.findFirst1ByTypeOrderByIdDesc(PostType.valueOf(type));
    }

    @Cacheable("posts")
    public PageContentDto<Post> findAllPosts(Integer page, Integer itemsPerPage, String type) {
        Integer perPage = Optional.ofNullable(itemsPerPage).orElse(CONTENT_PER_PAGE);
        PageRequest pageRequest = PageRequest.of(page, perPage, Sort.by(Sort.Direction.DESC, "id"));
        Page<Post> postPage = postRepository.findAllByType(pageRequest, PostType.valueOf(type));
        return new PageContentDto<>(
                postPage.getContent(),
                pageRequest.getPageNumber(),
                postPage.getTotalPages()
        );
    }

    @Cacheable(value = "post", key = "#id")
    public Post findById(Long id) throws EntityNotFoundException {
        Post post = postRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Post with id = %d not found", id))
        );
        post.setComments(
                Optional.ofNullable(post.getComments())
                        .orElse(new ArrayList<>())
                        .stream()
                        .filter(Comments::isActive)
                        .collect(Collectors.toList())
        );
        return post;
    }

    @Cacheable(value = "postRaw", key = "#id")
    public Post findByIdRawComments(Long id) throws EntityNotFoundException {
        return postRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Post with id = %d not found", id))
        );
    }


    @Caching(
            evict = {
                    @CacheEvict(value = "posts", allEntries = true),
                    @CacheEvict(value = "grid", allEntries = true),
                    @CacheEvict(value = "post", key = "#id"),
                    @CacheEvict(value = "postRaw", key = "#id")
            }
    )
    public void deletePost(Long id) {
        logger.info("Delete post with id {}", id);
        UrlHelper helper = UrlHelper.getPaths(id, uploadPath, "posts");
        uploadService.removeAll(helper.getPathToUpload());
        postRepository.deleteById(id);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "posts", allEntries = true),
                    @CacheEvict(value = "grid", allEntries = true)
            },
            put = {
                    @CachePut(value = "post", key = "#id"),
                    @CachePut(value = "postRaw", key = "#id")
            }
    )
    public Post updatePost(String title,
                           String description,
                           String shortDescription,
                           MultipartFile titleImage,
                           MultipartFile titleImageMini,
                           MultipartFile[] postImages,
                           String type,
                           Long id) throws EntityNotFoundException {
        logger.info("Update post with id={}, description={}, shortDescription={}, type={}, images count={}", id, description, shortDescription, type, postImages.length);
        Post post = findById(id);

        UrlHelper helper = UrlHelper.getPaths(post.getId(), uploadPath, "posts");

        Optional.ofNullable(title).ifPresent(post::setTitle);
        Optional.ofNullable(shortDescription).ifPresent(post::setShortDescription);
        Optional.ofNullable(type).ifPresent(t -> post.setType(PostType.valueOf(t)));


        Optional.ofNullable(titleImage).ifPresent(image -> {
            try {
                String path = helper.getPathToUpload() + "/title";
                uploadService.removeOld(path);
                post.setTitleImage(uploadService.uploadImage(image, path, helper.getURL() + "/title"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Optional.ofNullable(titleImageMini).ifPresent(image -> {
            try {
                String path = helper.getPathToUpload() + "/title_short";
                uploadService.removeOld(path);
                post.setTitleImageMini(uploadService.uploadImage(image, path, helper.getURL() + "/title_short"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Optional.ofNullable(description).ifPresent(d -> {
            String path = helper.getPathToUpload() + "/description";
            try {
                uploadService.removeOld(path);
                post.setDescription(processDescription(d, postImages, path, helper.getURL() + "/description"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return postRepository.save(post);
    }


    @Caching(
            evict = {
                    @CacheEvict(value = "posts", allEntries = true),
                    @CacheEvict(value = "grid",allEntries = true)
            }
    )
    public Post uploadPost(String title,
                           String description,
                           String shortDescription,
                           MultipartFile titleImage,
                           MultipartFile titleImageMini,
                           MultipartFile[] postImages,
                           String type) throws IOException {
        logger.info("Upload post with description={}, shortDescription={}, type={}, images count={}", description, shortDescription, type, postImages.length);
        PostType postType;
        try {
            postType = PostType.valueOf(type);
        } catch (Exception e) {
            throw new BadPostTypeException(String.format("Type %s is incorrect", type));
        }

        Post post = new Post();
        post.setShortDescription(shortDescription);
        post.setTime(LocalDateTime.now());
        post.setTitle(title);
        post.setType(postType);
        post = postRepository.save(post);

        uploadService.createPostFolders(uploadPath, post.getId());

        UrlHelper helper = UrlHelper.getPaths(post.getId(), uploadPath, "posts");
        String pathToUpload = helper.getPathToUpload();
        String URL = helper.getURL();

        post.setTitleImage(uploadService.uploadImage(titleImage, pathToUpload + "/title", URL + "/title"));
        post.setTitleImageMini(uploadService.uploadImage(titleImageMini, pathToUpload + "/title_short", URL + "/title_short"));

        post.setDescription(processDescription(description, postImages, pathToUpload + "/description", URL + "/description"));
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
                } else {
                    textBuilder.append(s);
                }
                i++;
            }
            return textBuilder.toString();
        }
        return description;
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "post", key = "#id"),
                    @CacheEvict(value = "postRaw", key = "#id")
            }
    )
    public Comments addComment(Long id, String nickname, String text) throws EntityNotFoundException {
        Comments comments = new Comments();
        comments.setActive(false);
        comments.setNickname(nickname);
        comments.setText(text);
        comments.setTime(LocalDateTime.now());
        comments.setPost(findById(id));
        return commentsRepository.save(comments);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "post", key = "#postId"),
                    @CacheEvict(value = "postRaw", key = "#postId")
            }
    )
    public void setActiveComments(boolean active, Long id, Long postId) {
        commentsRepository.setActive(active, id);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "post", key = "#postId"),
                    @CacheEvict(value = "postRaw", key = "#postId")
            }
    )
    public void deleteComments(Long id, Long postId) {
        commentsRepository.deleteById(id);
    }

    public Comments findCommentById(Long id) {
        return commentsRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("Entity comments with id = %d not found", id))
        );
    }

    @Cacheable("grid")
    public List<Grid> getGrid() {
        List<Grid> gridList = new ArrayList<>();
        PostType[] types = new PostType[]{RALLY,NEWS,MEMO,EVENTS};
        for (PostType type : types) {
            Post post = postRepository.findFirst1ByTypeOrderByIdDesc(type);
            Grid postToGrid = new Grid(
                    post.getTitleImageMini(),
                    post.getTitle(),
                    mapType(post.getType()),
                    extractUrl(post.getType()),
                    post.getId()
            );
            gridList.add(postToGrid);
        }
        GalleryImage image = albumService.getLastImageFromAll();
        gridList.add(new Grid(
                image.getUrl(),
                image.getAlbum().getName(),
                "Галерея",
                "/gallery",
                image.getAlbum().getId()
        ));
        return gridList;
    }
    private String extractUrl(PostType postType){
        String type = postType.toString();
        return type.equalsIgnoreCase("memo")?"/memories":"/"+type.toLowerCase();
    }
    private String mapType(PostType postType){
        switch (postType){
            case EVENTS:return "Мероприятия";
            case RALLY:return "Автопробеги";
            case NEWS:return "Новости";
            case MEMO:return "Воспоминания";
        }
        throw new RuntimeException("wrong post type");
    }
}
