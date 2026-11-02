package com.example.Booking.config;

import com.example.Booking.exception.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {

        Components components = new Components();

        // Register ErrorResponse schema
        ModelConverters.getInstance()
                .read(ErrorResponse.class)
                .forEach(components::addSchemas);

        // Register API key security
        components.addSecuritySchemes(
                "ApiKeyAuth",
                new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-Key")
        );

        // Register reusable error responses
        components
                .addResponses("401", errorResponse("Unauthorized"))
                .addResponses("400", errorResponse("Bad Request"))
                .addResponses("404", errorResponse("Not Found"))
                .addResponses("422", errorResponse("Unprocessable Content"))
                .addResponses("429", errorResponse("Too Many Requests"))
                .addResponses("409", errorResponse("Already Exists"))
                .addResponses("503", errorResponse("Service Unavailable"))
                .addResponses("500", errorResponse("Internal Server Error"));

        return new OpenAPI()
                .servers(List.of(
                        new Server()
                                .url("https://booking-api-ngrf.onrender.com")
                                .description("Production Server")
                ))
                .components(components)
                .addSecurityItem(
                        new SecurityRequirement().addList("ApiKeyAuth")
                );
    }

    private ApiResponse errorResponse(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                        "application/json",
                        new MediaType().schema(
                                new Schema<>().$ref(
                                        "#/components/schemas/ErrorResponse"
                                )
                        )
                ));
    }
}
