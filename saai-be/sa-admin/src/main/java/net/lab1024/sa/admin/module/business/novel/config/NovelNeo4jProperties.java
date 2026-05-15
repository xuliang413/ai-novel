package net.lab1024.sa.admin.module.business.novel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Neo4j 连接配置。
 *
 * 开发环境默认连接本地 Docker 中的 Neo4j，线上环境应通过环境变量覆盖密码。
 */
@Data
@Component
@ConfigurationProperties(prefix = "novel.neo4j")
public class NovelNeo4jProperties {

    /**
     * Neo4j Bolt 连接地址。
     */
    private String uri;

    /**
     * Neo4j 登录账号。
     */
    private String username;

    /**
     * Neo4j 登录密码。
     */
    private String password;

    /**
     * 是否在启动时自动创建图谱约束。
     */
    private boolean initSchema = true;
}
