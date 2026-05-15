package net.lab1024.sa.admin.module.business.novel.service;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelHealthVO;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 小说模块健康检查服务。
 *
 * 用于 M0 验收本地 Docker 中的 MySQL、Redis、Neo4j 是否都能被后端访问。
 */
@Service
public class NovelHealthService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private NovelGraphService novelGraphService;

    /**
     * 汇总检查三类中间件连接状态。
     */
    public NovelHealthVO check() {
        NovelHealthVO health = new NovelHealthVO();
        checkMysql(health);
        checkRedis(health);
        checkNeo4j(health);
        return health;
    }

    /**
     * 检查 MySQL 数据库连通性。
     */
    private void checkMysql(NovelHealthVO health) {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            health.setMysql(true);
            health.setMysqlMessage("ok");
        } catch (Exception e) {
            health.setMysql(false);
            health.setMysqlMessage(e.getMessage());
        }
    }

    /**
     * 检查 Redis 连通性。
     */
    private void checkRedis(NovelHealthVO health) {
        RedisConnection connection = null;
        try {
            connection = Objects.requireNonNull(stringRedisTemplate.getConnectionFactory()).getConnection();
            connection.ping();
            health.setRedis(true);
            health.setRedisMessage("ok");
        } catch (Exception e) {
            health.setRedis(false);
            health.setRedisMessage(e.getMessage());
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * 检查 Neo4j 连通性。
     */
    private void checkNeo4j(NovelHealthVO health) {
        try {
            novelGraphService.check();
            health.setNeo4j(true);
            health.setNeo4jMessage("ok");
        } catch (Exception e) {
            health.setNeo4j(false);
            health.setNeo4jMessage(e.getMessage());
        }
    }
}
