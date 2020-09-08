package com.test.zipkin.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import javax.annotation.Resource;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CloseableHttpClient httpClient;
    @GetMapping("/springmvc")
    public String echo() {
        return "springmvc";
    }

    @GetMapping("/redis")
    public String redis() {
        return stringRedisTemplate.opsForValue().get("test");
    }

    @GetMapping("/http")
    public String httpClient() throws IOException {
        HttpGet httpGet = new HttpGet("http://www.baidu.com");

        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet, HttpClientContext.create())){
            return EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/mysql")
    public Object mysql(Integer id) {
        try {
            return selectById(id);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyMap();
        }
    }

    private Object selectById(Integer id) {
        return jdbcTemplate.queryForMap("SELECT id, username, password FROM demo_user WHERE id = ?", id);
    }
}
