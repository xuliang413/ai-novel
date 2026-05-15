package net.lab1024.sa.admin.module.business.novel.config;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Neo4j Driver 配置。
 *
 * Driver 作为单例 Bean 复用连接池，应用关闭时由 Spring 调用 close 释放资源。
 */
@Configuration
public class NovelNeo4jConfig {

    @Resource
    private NovelNeo4jProperties properties;

    /**
     * 创建小说模块专用 Neo4j Driver。
     */
    @Bean(destroyMethod = "close")
    public Driver novelNeo4jDriver() {
        String uri = StringUtils.defaultIfBlank(properties.getUri(), "bolt://127.0.0.1:8687");
        String username = StringUtils.defaultIfBlank(properties.getUsername(), "neo4j");
        String password = StringUtils.defaultString(properties.getPassword());
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }
}
