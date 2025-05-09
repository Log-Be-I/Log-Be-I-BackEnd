package com.springboot;

import com.springboot.auth.utils.AdminMailProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@EnableAsync
@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties(AdminMailProperties.class)
public class LogBeIApplication {
  public static void main(String[] args) {
    new SpringApplicationBuilder(LogBeIApplication.class)
        .properties("spring.config.location=classpath:/application.yml")
        .run(args);
  }
    @PostConstruct
    public void setTimezone() {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
  }
}
