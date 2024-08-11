package dev.wcirou;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
public class ConvertImageType implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    //Creating Logger
    private static final Logger logger = LoggerFactory.getLogger(ConvertImageType.class);
    //Creating Class Wide S3 Client
    private static final S3Client s3 = S3Client.builder()
            .region(Region.US_EAST_1)
            .build();
    //Creating Services object to access methods
    private static final Services services = new Services();

    @Override
    public APIGatewayProxyResponseEvent handleRequest (APIGatewayProxyRequestEvent input, Context context){
    try {
        //Verifying request
        if (input.getPathParameters() == null || !input.getPathParameters().containsKey("imageKey") || !input.getPathParameters().containsKey("bucketName")) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody("Missing required parameters: imageName or bucketName");
        }
        //Getting Image Key and Bucket Name
        String imageKey = input.getPathParameters().get("imageKey");
        String bucketName = input.getPathParameters().get("bucketName");
        //Retrieving Image from S3 Bucket
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(imageKey)
                .build();
        //Creating Buffered Image
        ResponseInputStream<GetObjectResponse> object = s3.getObject(getObjectRequest);
        BufferedImage bufferedImage = ImageIO.read(object);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        //Getting Image Type
        String imageType = object.response().contentType();
        //Performing Conversion based on Image Type
        if (imageType.contains("jpeg")|| imageType.contains("jpg")) {
            return services.convertJPEGToPNGThenUploadToS3(bufferedImage,imageKey,outputStream,s3,bucketName);
        } else if (imageType.contains("png")) {
            return services.convertPNGToJPEGThenUploadToS3(bufferedImage, imageKey, outputStream, s3, bucketName);
        }else {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody("Image not of Type png or jpg");
        }
    }catch (Exception e){
        logger.error("Error processing request", e);
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withBody("Error processing request");
    }
}

}

