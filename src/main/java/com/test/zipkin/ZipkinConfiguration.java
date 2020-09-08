package com.test.zipkin;

import java.util.Collections;

import javax.servlet.Filter;

import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.web.client.RestTemplate;

import brave.CurrentSpanCustomizer;
import brave.SpanCustomizer;
import brave.Tracing;
import brave.http.HttpTracing;
import brave.httpclient.TracingHttpClientBuilder;
import brave.opentracing.BraveTracer;
import brave.servlet.TracingFilter;
import brave.spring.web.TracingClientHttpRequestInterceptor;
import io.opentracing.Tracer;
import io.opentracing.contrib.redis.common.TracingConfiguration;
import io.opentracing.contrib.redis.spring.data.connection.TracingRedisConnectionFactory;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

@Configuration
public class ZipkinConfiguration {

    @Bean
    public Sender sender() {
        return OkHttpSender.create("http://127.0.0.1:9411/api/v2/spans");
    }

    @Bean
    public AsyncReporter<Span> spanReporter() {
        return AsyncReporter.create(sender());
    }

    @Bean
    public Tracing tracing(@Value("${spring.application.name}") String serviceName) {
        return Tracing.newBuilder()
                .localServiceName(serviceName)
                .addSpanHandler(AsyncZipkinSpanHandler.create(spanReporter()))
                .build();
    }

    /**
     * allows someone to add tags to a span if a trace is in process
     */
    @Bean
    public SpanCustomizer spanCustomizer(Tracing tracing) {
        return CurrentSpanCustomizer.create(tracing);
    }

    // ==================== HTTP 相关 ====================
    @Bean
    public HttpTracing httpTracing(Tracing tracing) {
        return HttpTracing.create(tracing);
    }

    @Bean
    public Filter tracingFilter(HttpTracing httpTracing) {
        return TracingFilter.create(httpTracing);
    }

    @Bean
    public Tracer openTracer(Tracing tracing) {
        return BraveTracer.create(tracing);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(Tracer tracer, RedisProperties redisProperties) {
        // 创建 JedisConnectionFactory 对象
        RedisConnectionFactory connectionFactory = new JedisConnectionFactory();
        // 创建 TracingConfiguration 对象
        TracingConfiguration tracingConfiguration = new TracingConfiguration.Builder(tracer)
                // 设置拓展 Tag ，设置 Redis 服务器地址。因为默认情况下，不会在操作 Redis 链路的 Span 上记录 Redis 服务器的地址，所以这里需要设置。
                .extensionTag("Server Address", redisProperties.getHost() + ":" + redisProperties.getPort())
                .build();
        // 创建 TracingRedisConnectionFactory 对象
        return new TracingRedisConnectionFactory(connectionFactory, tracingConfiguration);
    }

    @Bean
    public RestTemplate restTemplate(Tracing tracing) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(TracingClientHttpRequestInterceptor.create(tracing)));
        return restTemplate;
    }

    @Bean
    public HttpClient httpClient(Tracing tracing) {
        return TracingHttpClientBuilder.create(tracing).build();
    }
}
