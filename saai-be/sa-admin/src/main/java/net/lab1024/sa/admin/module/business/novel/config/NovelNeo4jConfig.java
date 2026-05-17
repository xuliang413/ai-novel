package net.lab1024.sa.admin.module.business.novel.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Neo4j 驱动配置。
 * <p>
 * 只负责创建官方 Driver, 业务层统一通过 NovelGraphService 的白名单模板访问图数据库。
 *
 * @Author AI-Novel
 */
@Configuration
public class NovelNeo4jConfig {

    /**
     * 创建 Neo4j Driver。
     * <p>
     * Driver 是线程安全对象, Spring 容器里保留一个单例即可; 真正的会话在业务方法中按次创建和关闭。
     *
     * @param uri Neo4j Bolt 地址
     * @param username Neo4j 用户名
     * @param password Neo4j 密码
     * @return 官方 Neo4j Java Driver
     */
    @Bean
    public Driver neo4jDriver(@Value("${neo4j.uri:bolt://localhost:7687}") String uri,
                              @Value("${neo4j.username:neo4j}") String username,
                              @Value("${neo4j.password:password}") String password) {
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }
}
