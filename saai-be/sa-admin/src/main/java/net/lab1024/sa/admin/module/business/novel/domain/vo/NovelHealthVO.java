package net.lab1024.sa.admin.module.business.novel.domain.vo;

import lombok.Data;

/**
 * 小说模块健康检查返回对象。
 */
@Data
public class NovelHealthVO {

    /**
     * MySQL 是否连通。
     */
    private Boolean mysql;

    /**
     * Redis 是否连通。
     */
    private Boolean redis;

    /**
     * Neo4j 是否连通。
     */
    private Boolean neo4j;

    /**
     * MySQL 检查结果说明。
     */
    private String mysqlMessage;

    /**
     * Redis 检查结果说明。
     */
    private String redisMessage;

    /**
     * Neo4j 检查结果说明。
     */
    private String neo4jMessage;
}
