package com.trelloiii.honor.services;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class UploadService {

    @Value("${hostname}")
    private String hostname;

    public void createFolders(String uploadPath, Long id) {
        String[] folders={"description","title","title_short"};
        for (String folder : folders) {
            new File(String.join("/",uploadPath,"posts",String.valueOf(id),folder)).mkdirs();
        }
    }

    /**
     *
     * @param image downloaded image
     * @param pathToUpload where to upload
     * @param resPrefix relative path from upload path
     * @return URL for image
     * @throws IOException
     */
    public String uploadImage(MultipartFile image, String pathToUpload, String resPrefix) throws IOException {
        String imageName=image.getOriginalFilename();
        String fileUUID= UUID.randomUUID().toString();
        String resultImageName = resPrefix + "/" + fileUUID + "_" + imageName;
        String uploadImageName = pathToUpload + "/" + fileUUID + "_" + imageName;
        image.transferTo(new File(uploadImageName));
        return String.join("/",hostname,"img",resultImageName);
    }
    public void removeOld(String path) throws IOException {
        FileUtils.cleanDirectory(new File(path));
    }
}