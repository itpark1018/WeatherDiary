package com.example.weatherdiary.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@OpenAPIDefinition(
        info = @Info(title = "날씨 일기 프로젝트 :)",
                description = "날씨 일기를 CRUD 할수 있는 백엔드 API 입니다.",
                version = "v1"))
@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi chatOpenApi() {
        String[] paths = {"/**"};

        return GroupedOpenApi.builder()
                .group("날씨 일기 API v1")
                .pathsToMatch(paths)
                .build();
    }
}
