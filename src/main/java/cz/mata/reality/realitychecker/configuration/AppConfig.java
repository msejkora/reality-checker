package cz.mata.reality.realitychecker.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/*
 * @created 01/10/2021 - 12:48
 * @project RealityChecker
 * @author msejkora
 */
@Slf4j
@Data
@Configuration
public class AppConfig {
    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}
