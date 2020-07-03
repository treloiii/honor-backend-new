package com.trelloiii.honor;

import com.trelloiii.honor.exceptions.EntityNotFoundException;
import com.trelloiii.honor.model.Comments;
import com.trelloiii.honor.model.Post;
import com.trelloiii.honor.model.PostType;
import com.trelloiii.honor.services.PostService;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.IOUtils;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;

@SpringBootTest
@RunWith(SpringRunner.class)
class PostTests {

    @Autowired
    private PostService postService;
    @SneakyThrows
    @Test
    @Transactional
    public void uploadUpdateDeletePostsTest(){
        String title="Test title";
        String description="Test description";
        String shortDescription="Test short description";
        PostType type=PostType.NEWS;
        File mockImage=new File("src/test/resources/mock.png");
        MultipartFile titleImage=new MockMultipartFile("title_image",
                mockImage.getName(),
                "image/png",
                IOUtils.readAllBytes(
                    new FileInputStream(mockImage)
                )
        );
        Post post=postService.uploadPost(title,description,shortDescription,titleImage,titleImage,new MultipartFile[]{titleImage},type.toString());
        Assert.assertNotNull(post.getTitleImage());
        Assert.assertTrue(post.getTitleImage().contains("title"));
        Assert.assertNotNull(post.getTitleImageMini());
        Assert.assertTrue(post.getTitleImageMini().contains("title_short"));
        Assert.assertTrue(post.getDescription().contains("http://localhost:8080"));
        Assert.assertTrue(post.getDescription().contains("mock.png"));
        Assert.assertTrue(post.getDescription().contains("description"));
        Assert.assertEquals(title,post.getTitle());
        Assert.assertEquals(shortDescription,post.getShortDescription());

        updatingPostTest(post.getId());
        deletePostTest(post.getId());
    }


    @SneakyThrows
    @Transactional
    public void updatingPostTest(Long id){
        String title="new title";
        String description="new description";
        String type="MEMORIES";
        String shortDescription="new short";
        Post post=postService.updatePost(
                title,
                description,
                shortDescription,
                null,
                null,
                new MultipartFile[0],
                type,
                id
        );
        Assert.assertEquals(title,post.getTitle());
        Assert.assertEquals(description,post.getDescription());
        Assert.assertEquals(shortDescription,post.getShortDescription());
        Assert.assertEquals(type,post.getType().toString());
        Assert.assertNotNull(post.getTitleImageMini());
        Assert.assertNotNull(post.getTitleImage());
    }

    public void deletePostTest(Long id){
        postService.deletePost(id);
        Assert.assertThrows(EntityNotFoundException.class,()-> postService.findById(id));
        Assert.assertFalse(new File(String.format("./data/posts/%d",id)).exists());
    }

    @Test
    @Transactional
    public void postCommentsAddRedactRemoveTest(){
        Post post=postService.getLastByType(PostType.NEWS.toString());
        String nickname="mock nickname";
        String text="mock text";
        Comments comments = postService.addComment(post.getId(),nickname,text);
        Assert.assertEquals(nickname,comments.getNickname());
        Assert.assertEquals(text,comments.getText());
        Assert.assertFalse(comments.isActive());
        testDeleteComment(comments.getId());
    }
    public void testDeleteComment(Long id){
        postService.deleteComments(id,0L);
        Assert.assertThrows(EntityNotFoundException.class,()->postService.findCommentById(id));
    }
}
