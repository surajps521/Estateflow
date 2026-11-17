package com.realestate.crm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
@Configuration
class ApiDocumentation {
 @Bean OpenAPI crmOpenApi(){return new OpenAPI().info(new Info().title("Real Estate CRM API").version("v1").description("JWT-secured Real Estate CRM API"))
 .components(new io.swagger.v3.oas.models.Components().addSecuritySchemes("bearerAuth",new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));}
}
