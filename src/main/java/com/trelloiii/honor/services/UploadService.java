package com.trelloiii.honor.services;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.UUID;

@Service
public class UploadService {

    @Value("${hostname}")
    private String hostname;
    private final Logger logger = LoggerFactory.getLogger(UploadService.class);
    public void createPostFolders(String uploadPath, Long id) {
        String[] folders={"description","title","title_short"};
        for (String folder : folders) {
            new File(String.join("/",uploadPath,"posts",String.valueOf(id),folder)).mkdirs();
        }
    }

    public void createOrdensFolders(String uploadPath, Long id){
        new File(String.join("/",uploadPath,"ordens",String.valueOf(id))).mkdirs();
    }
    public void createGalleryFolders(String uploadPath,Long id){
        new File(String.join("/",uploadPath,"gallery",String.valueOf(id))).mkdirs();
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
        File resultedImage = new File(uploadImageName);
        image.transferTo(resultedImage);
        compressImage(resultedImage);
        return String.join("/",hostname,"img",resultImageName);
    }
    public void removeOld(String path) throws IOException {
        FileUtils.cleanDirectory(new File(path));
    }
    public void removeAll(String path){
        try {
            FileUtils.deleteDirectory(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void removeFile(String path) throws IOException {
        FileUtils.forceDelete(new File(path));
    }
    private void compressImage(File input) {
        try {
            if(FileUtils.sizeOf(input)/1024>350){
                logger.info("Image {} are smaller when 350kb, ignore compression",FilenameUtils.getBaseName(input.getAbsolutePath()));
                return;
            }
            String fileExtension = FilenameUtils.getExtension(input.getAbsolutePath());
            if (fileExtension.equals("png") || fileExtension.equals("PNG"))
                return; //Compress png via jpeg is hell for OpenJDK
            BufferedImage image = ImageIO.read(input);

            OutputStream os = new FileOutputStream(input);

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = writers.next();

            ImageOutputStream ios = ImageIO.createImageOutputStream(os);
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.4f);  // Change the quality value you prefer
            writer.write(null, new IIOImage(image, null, null), param);
            os.close();
            ios.close();
            writer.dispose();
        }
        catch (Exception e){
            logger.warn("Cannot compress image {}",FilenameUtils.getBaseName(input.getAbsolutePath()));
        }
    }
}
