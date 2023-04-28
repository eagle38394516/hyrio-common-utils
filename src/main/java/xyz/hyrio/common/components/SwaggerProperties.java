package xyz.hyrio.common.components;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperties {
    private boolean enabled = false;
    private String title;
    private String description;
    private String version;
}
