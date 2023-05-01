package xyz.hyrio.common.components;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperties {
    private boolean enabled = false;
    private String title;
    private String description;
    private String version;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "SwaggerProperties{" +
               "enabled=" + enabled +
               ", title='" + title + '\'' +
               ", description='" + description + '\'' +
               ", version='" + version + '\'' +
               '}';
    }
}
