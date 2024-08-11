package dev.wcirou;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Services {
    public APIGatewayProxyResponseEvent convertJPEGToPNGThenUploadToS3(BufferedImage bufferedImage, String imageKey, ByteArrayOutputStream outputStream, S3Client s3, String bucketName) throws IOException {
        // Check for alpha channel
        if (bufferedImage.getColorModel().hasAlpha()) {
            // Convert image to ARGB, removing alpha channel
            BufferedImage rgbImage = new BufferedImage(
                    bufferedImage.getWidth(),
                    bufferedImage.getHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );
            rgbImage.createGraphics().drawImage(bufferedImage, 0, 0, null);
            bufferedImage = rgbImage;
        }
        //Converting Image from JPEG to PNG
        boolean wasSuccessful = ImageIO.write(bufferedImage, "png", outputStream);
        if (!wasSuccessful){
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("Error converting image");
        }
        //Turning New Image into a Byte Array to be uploaded back to S3
        byte[] pngData = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(pngData);
        //Building Put Object Request and the Request Body to pass as parameters to the S3 Client, and changing the Key to the correct type
        if (imageKey.contains("jpeg")){
            imageKey = imageKey.replace(".jpeg", ".png");
        }else if(imageKey.contains("jpg")){
            imageKey = imageKey.replace(".jpg", ".png");
        }
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key("converted/" + imageKey)
                .contentType("image/png")
                .build();
        RequestBody requestBody = RequestBody.fromInputStream(inputStream, pngData.length);
        //Passing Parameters to the S3 client and Returning Successful Response if Successful
        PutObjectResponse response = s3.putObject(putObjectRequest, requestBody);
        if (response.sdkHttpResponse().isSuccessful()) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody("Image converted successfully");
        }else{
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("Error converting image");
        }
    }
    public APIGatewayProxyResponseEvent convertPNGToJPEGThenUploadToS3(BufferedImage bufferedImage, String imageKey, ByteArrayOutputStream outputStream, S3Client s3, String bucketName) throws IOException {
        // Check for alpha channel
        if (bufferedImage.getColorModel().hasAlpha()) {
            // Convert image to RGB, removing alpha channel
            BufferedImage rgbImage = new BufferedImage(
                    bufferedImage.getWidth(),
                    bufferedImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );
            rgbImage.createGraphics().drawImage(bufferedImage, 0, 0, null);
            bufferedImage = rgbImage;
        }
        //Converting Image from PNG to JPEG
        boolean wasSuccessful = ImageIO.write(bufferedImage, "jpg", outputStream);
        if (!wasSuccessful){
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("Error converting image");
        }
        //Turning New Image into a Byte Array to be uploaded back to S3
        byte[] jpgData = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(jpgData);
        //Building Put Object Request and the Request Body to pass as parameters to the S3 Client
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key("converted/" + imageKey.replace(".png", ".jpg"))
                .contentType("image/jpeg")
                .build();
        RequestBody requestBody = RequestBody.fromInputStream(inputStream, jpgData.length);
        //Passing Parameters to the S3 client and Returning Successful Response if Successful
        PutObjectResponse response = s3.putObject(putObjectRequest, requestBody);
        if (response.sdkHttpResponse().isSuccessful()) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody("Image converted successfully");
        }else{
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("Error converting image");
        }
    }


}
