package com.augenda.services.templateservice.resources.configs

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger.web.InMemorySwaggerResourcesProvider
import springfox.documentation.swagger.web.SwaggerResource
import springfox.documentation.swagger.web.SwaggerResourcesProvider

@Configuration
class SwaggerSpecConfig(
    @Value("\${spring.application.name}")
    val applicationName: String,
    @Value("\${openapi.location}")
    val location: String,
) {

    @Primary
    @Bean
    fun swaggerResourcesProvider(defaultResourcesProvider: InMemorySwaggerResourcesProvider): SwaggerResourcesProvider? {
        return SwaggerResourcesProvider {
            val wsResource = SwaggerResource()
            wsResource.name = applicationName
            wsResource.swaggerVersion = "2.0"
            wsResource.location = location
            val resources: MutableList<SwaggerResource> =
                ArrayList()
            resources.add(wsResource)
            resources
        }
    }

    @Bean
    fun swagger(): Docket? {
        return Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.any())
            .build()
    }
}
