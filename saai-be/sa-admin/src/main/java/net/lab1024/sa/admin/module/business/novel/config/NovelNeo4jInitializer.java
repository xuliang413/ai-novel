package net.lab1024.sa.admin.module.business.novel.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphNodeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphPropertyEnum;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 小说 Neo4j 初始化器。
 *
 * 应用启动时自动创建 M0 所需的唯一约束，保证本地 Docker、测试环境和部署环境的图谱结构一致。
 */
@Component
@Slf4j
public class NovelNeo4jInitializer {

    /**
     * M0 图谱约束语句。
     *
     * 约束遵循技术方案里的图谱隔离原则：标签只表达实体类型，项目归属统一通过 projectId 属性表达。
     */
    private static final List<String> INIT_CYPHER_LIST = List.of(
            """
            CREATE CONSTRAINT novel_project_project_id IF NOT EXISTS
            FOR (p:%s) REQUIRE p.%s IS UNIQUE
            """.formatted(label(NovelGraphNodeEnum.PROJECT), property(NovelGraphPropertyEnum.PROJECT_ID)),
            """
            CREATE CONSTRAINT novel_character_project_name IF NOT EXISTS
            FOR (c:%s) REQUIRE (c.%s, c.%s) IS UNIQUE
            """.formatted(label(NovelGraphNodeEnum.CHARACTER), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME)),
            """
            CREATE CONSTRAINT novel_location_project_name IF NOT EXISTS
            FOR (l:%s) REQUIRE (l.%s, l.%s) IS UNIQUE
            """.formatted(label(NovelGraphNodeEnum.LOCATION), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME)),
            """
            CREATE CONSTRAINT novel_clue_project_name IF NOT EXISTS
            FOR (c:%s) REQUIRE (c.%s, c.%s) IS UNIQUE
            """.formatted(label(NovelGraphNodeEnum.CLUE), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME)),
            """
            CREATE CONSTRAINT novel_chapter_project_number IF NOT EXISTS
            FOR (c:%s) REQUIRE (c.%s, c.%s) IS UNIQUE
            """.formatted(label(NovelGraphNodeEnum.CHAPTER), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NUMBER))
    );

    @Resource
    private Driver novelNeo4jDriver;

    @Resource
    private NovelNeo4jProperties properties;

    /**
     * 初始化 M0 图谱约束。
     *
     * Neo4j 如果暂时不可用，后端仍允许启动；后续可以通过 /novel/health 明确看到 Neo4j 状态。
     */
    @PostConstruct
    public void initSchema() {
        if (!properties.isInitSchema()) {
            log.info("AI 小说 Neo4j 自动初始化已关闭");
            return;
        }

        try (Session session = novelNeo4jDriver.session()) {
            for (String cypher : INIT_CYPHER_LIST) {
                session.run(cypher).consume();
            }
            log.info("AI 小说 Neo4j M0 图谱约束初始化完成");
        } catch (Exception e) {
            log.warn("AI 小说 Neo4j M0 图谱约束初始化失败，请检查 Neo4j 服务和账号密码配置：{}", e.getMessage(), e);
        }
    }

    private static String label(NovelGraphNodeEnum nodeEnum) {
        return nodeEnum.label();
    }

    private static String property(NovelGraphPropertyEnum propertyEnum) {
        return propertyEnum.key();
    }
}
