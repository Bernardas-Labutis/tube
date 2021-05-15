package lt.vu.tube.config;

import lt.vu.tube.interceptor.UserActivityLogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    private UserActivityLogInterceptor userActivityLogInterceptor;

    @Autowired
    public InterceptorConfig(UserActivityLogInterceptor userActivityLogInterceptor){
        this.userActivityLogInterceptor = userActivityLogInterceptor;
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userActivityLogInterceptor);
    }
}