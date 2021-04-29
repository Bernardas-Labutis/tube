package lt.vu.tube.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class VideoConfig {
    @Value("#{${tube.valid-mime-types}}")
    private List<String> validMimeTypes;

    public List<String> getValidMimeTypes() {
        return validMimeTypes;
    }
}
