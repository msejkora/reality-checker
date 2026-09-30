package cz.mata.reality.realitychecker.configuration;


import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;


@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @NotEmpty
    private String filterDirectory;
}
