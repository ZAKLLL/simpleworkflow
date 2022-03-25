package com.zakl.workflow.config

import cn.hutool.core.collection.ListUtil
import com.google.common.collect.Lists
import io.swagger.annotations.Api
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.swagger2.annotations.EnableSwagger2
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.spi.DocumentationType
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.builders.PathSelectors
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.swagger.web.UiConfiguration
import springfox.documentation.swagger.web.UiConfigurationBuilder
import springfox.documentation.swagger.web.ModelRendering
import springfox.documentation.swagger.web.DocExpansion
import springfox.documentation.swagger.web.OperationsSorter
import springfox.documentation.swagger.web.TagsSorter
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.service.*
import java.util.ArrayList

/**
 * SwaggerConfig
 *
 * @author ZJK
 * @since 2020/12/02
 */
@Configuration
@EnableSwagger2
class SwaggerConfig {
    @Bean
    fun createRestApi(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo())
            .enable(true)
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(Api::class.java))
            .paths(PathSelectors.any())
            .build()
            .securityContexts(securityContexts()) //配置token訪問
            .securitySchemes(ListUtil.of(ApiKey("auth-token", "auth-token", "header")))
    }

    //auth-token
    private fun securityContexts(): List<SecurityContext> {
        val securityContexts: MutableList<SecurityContext> = ArrayList()
        securityContexts.add(
            SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.any())
                .build()
        )
        return securityContexts
    }

    fun defaultAuth(): List<SecurityReference> {
        val authorizationScope = AuthorizationScope("global", "accessEverything")
        val authorizationScopes = arrayOfNulls<AuthorizationScope>(1)
        authorizationScopes[0] = authorizationScope
        return Lists.newArrayList(
            SecurityReference("auth-token", authorizationScopes)
        )
    }

    @Bean
    fun uiConfig(): UiConfiguration {
        return UiConfigurationBuilder.builder()
            .deepLinking(true)
            .displayOperationId(false)
            .defaultModelsExpandDepth(1)
            .defaultModelExpandDepth(1)
            .defaultModelRendering(ModelRendering.EXAMPLE)
            .displayRequestDuration(false)
            .docExpansion(DocExpansion.NONE)
            .filter(false)
            .maxDisplayedTags(null)
            .operationsSorter(OperationsSorter.ALPHA)
            .showExtensions(false)
            .tagsSorter(TagsSorter.ALPHA)
            .supportedSubmitMethods(UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS)
            .validatorUrl(null)
            .build()
    }

    private fun apiInfo(): ApiInfo {
        return ApiInfoBuilder() //页面标题
            .title("simpleWorkFlow 使用 Swagger2 构建 API 文档") //创建人
            .contact(Contact("zakl", "http://120.76.62.44//", "1015849735@qq.com")) //版本号
            .version("1.0") //描述
            .description("simpleWorkFlowWebApi API")
            .build()
    }
}