package com.hanfei.flashsales.config;

import com.hanfei.flashsales.utils.UserArgumentResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author: harris
 * @time: 2023
 * @summary: flash-sales
 */
@Slf4j
@EnableWebMvc // Without this annotation, overriding methods in WebMvcConfigurer will have no effect
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserArgumentResolver userArgumentResolver;

    /**
     * Add custom argument resolvers to the resolver list
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // userArgumentResolver: Retrieve User objects from UserService using ticket obtained from cookies
        resolvers.add(userArgumentResolver);
        log.info("Added custom argument resolver: UserArgumentResolver");
    }

    /**
     * Configure the handling of static resources
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map all paths to static resources located in classpath:/static/
        // addResourceHandler("/**") maps all request paths to static resources
        // addResourceLocations("classpath:/static/") maps the location of static resources to classpath:/static/
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }
}
