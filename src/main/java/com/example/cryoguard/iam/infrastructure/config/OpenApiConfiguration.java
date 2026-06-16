package com.example.cryoguard.iam.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CryoGuard API")
                        .version("1.0")
                        .description("CryoGuard Monitoring System API"))
                .servers(List.of(
                        new Server().url("https://lou1u2b31ub23ysaydqd6w621bqw.croswell.pe").description("Production"),
                        new Server().url("http://localhost:8080").description("Local")
                ));
    }
}
