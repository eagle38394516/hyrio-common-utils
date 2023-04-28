package xyz.hyrio.common.components;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("${swagger.enabled:false}")
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI(SwaggerProperties swaggerProperties) {
        return new OpenAPI().info(
                new Info().title(swaggerProperties.getTitle())
                        .description(swaggerProperties.getDescription())
                        .version(swaggerProperties.getVersion())
        );
    }
}
