package com.trelloiii.honor;

import com.trelloiii.honor.exceptions.EntityNotFoundException;
import com.trelloiii.honor.model.Ordens;
import com.trelloiii.honor.services.OrdenService;
import lombok.SneakyThrows;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.IOUtils;

import java.io.File;
import java.io.FileInputStream;

@SpringBootTest
@RunWith(SpringRunner.class)
class OrdenTests {

    @Autowired
    private OrdenService ordenService;

    @SneakyThrows
    @Test
    public void uploadUpdateDeleteOrdenTest() {
        String name = "mock orden";
        String description = "mock description";
        String shortDescription = "mock short";
        File mockImage = new File("src/test/resources/mock.png");
        MultipartFile titleImage = new MockMultipartFile("title_image",
                mockImage.getName(),
                "image/png",
                IOUtils.readAllBytes(
                        new FileInputStream(mockImage)
                )
        );
        Ordens orden = ordenService.addOrden(name, description, shortDescription, titleImage);
        assertEquals(name, orden.getName());
        assertEquals(description, orden.getDescription());
        assertEquals(shortDescription, orden.getShortDescription());
        assertTrue(orden.getTitleImage().contains("http://localhost:8080"));
        assertTrue(orden.getTitleImage().contains("mock.png"));
        updateOrdenTest(orden.getId(),orden.getDescription());
        deleteOrdenTest(orden.getId());
    }

    public void updateOrdenTest(Long id, String oldDescription) {
        String name = "new mock name";
        String shortDescription = "new short";
        Ordens orden = ordenService.updateOrden(name, null, shortDescription, null, id);
        assertEquals(name,orden.getName());
        assertEquals(shortDescription,orden.getShortDescription());
        assertEquals(oldDescription,orden.getDescription());
        assertNotNull(orden.getTitleImage());
    }

    public void deleteOrdenTest(Long id){
        ordenService.deleteOrden(id);
        Assert.assertThrows(EntityNotFoundException.class,()-> ordenService.findById(id));
        Assert.assertFalse(new File(String.format("./data/ordens/%d",id)).exists());
    }
}
