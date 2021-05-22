package lt.vu.tube.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import lt.vu.tube.model.LambdaResponse;
import lt.vu.tube.model.MediaTypeResponseBody;

public interface FunctionService {
    //Key yra path to file
    LambdaResponse<MediaTypeResponseBody> getMediaType(String key) throws JsonProcessingException;
}
