package dev.wcirou;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ImageConverterTest {
    @Mock
    APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
    @Mock
    Context context;
    @Mock
    LambdaLogger logger;
    @InjectMocks
    ConvertImageType handler;
    @BeforeEach
    public void initialize_context_logger_and_handler() {
        context = mock(Context.class);
        logger = mock(LambdaLogger.class);
        handler = new ConvertImageType();
        when(context.getLogger()).thenReturn(logger);
    }
    @Test
    public void ConvertImageTypeTest(){
        Map<String, String> pathParameters = Map.of("imageKey", "image002c290d-6d11-472b-93fb-7ad79bf75a32", "bucketName", "wcirouimageprocessorbucket");
        input.setPathParameters(pathParameters);
        APIGatewayProxyResponseEvent response = handler.handleRequest(input, context);
        System.out.println(response.getBody());
    }

}
