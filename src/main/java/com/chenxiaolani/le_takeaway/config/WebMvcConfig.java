package com.chenxiaolani.le_takeaway.config;

import com.chenxiaolani.le_takeaway.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    // 配置静态资源文件路径
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始静态资源文件路径的映射");
        registry.addResourceHandler("/backend/**")
                .addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**")
                .addResourceLocations("classpath:/front/");
    }

    /**
     * 扩展默认的消息转换器，让它可以用JacksonObjectMapper这个java对象转换器
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("开始扩展消息转换器");
        // 创建新的消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        // 设置对象消息转换器，这里使用我们自己的JacksonObjectMapper
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        // 将新的消息转换器添加到converters中,注意这里使用有索引的方法，优先使用我们自己的消息转换器
        converters.add(0, messageConverter);
    }
}
