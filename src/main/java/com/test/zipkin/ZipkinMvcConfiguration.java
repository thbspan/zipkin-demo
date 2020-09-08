package com.test.zipkin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import brave.spring.webmvc.SpanCustomizingAsyncHandlerInterceptor;

@Import(SpanCustomizingAsyncHandlerInterceptor.class)
@Configuration
public class ZipkinMvcConfiguration implements WebMvcConfigurer {
    @Autowired
    private SpanCustomizingAsyncHandlerInterceptor webMvcTracingCustomizer;

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        // <mvc:default-servlet-handler />
        configurer.enable();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(webMvcTracingCustomizer);
    }
}
