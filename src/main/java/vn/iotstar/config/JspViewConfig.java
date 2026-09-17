package vn.iotstar.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
public class JspViewConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:E:/upload}")
    private String uploadDir;

    @Bean
    public InternalResourceViewResolver jspViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        resolver.setViewNames("admin", "web", "admin/*");
        resolver.setOrder(0);
        return resolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();

        registry.addResourceHandler("/category/**")
                .addResourceLocations(
                        directoryLocation(uploadRoot.resolve("category")));

        registry.addResourceHandler("/product/**")
                .addResourceLocations(
                        directoryLocation(uploadRoot.resolve("product")));
    }

    private String directoryLocation(Path path) {
        String location = path.toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }
}
