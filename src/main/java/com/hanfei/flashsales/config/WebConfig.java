package com.hanfei.flashsales.config;

import com.hanfei.flashsales.utils.UserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * MVC 配置类
 *
 * @author: harris
 * @time: 2023
 * @summary: seckill
 */
@Configuration
@EnableWebMvc // 启用 Spring MVC 的配置，若无此注解，重写 WebMvcConfigurer 中的方法无效
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserArgumentResolver userArgumentResolver;

    /**
     * 添加自定义的参数解析器到解析器列表中
     *
     * @param resolvers 参数解析器列表
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // userArgumentResolver: 通过从 Cookie 中获取 userTicket，并通过该标识从 UserService 中获取对应的 User 对象
        resolvers.add(userArgumentResolver);
    }

    /**
     * 配置静态资源的处理
     *
     * @param registry 静态资源注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将所有路径都映射到 classpath:/static/ 目录下，以便于展示前端静态资源
        // addResourceHandler("/**") 表示将所有的请求路径都映射到静态资源
        // addResourceLocations("classpath:/static/") 表示将静态资源的位置映射到 classpath:/static/ 目录下
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }
}
