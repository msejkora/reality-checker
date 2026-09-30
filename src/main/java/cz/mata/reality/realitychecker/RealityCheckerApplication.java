package cz.mata.reality.realitychecker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RealityCheckerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RealityCheckerApplication.class, args);
    }

}
