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
 * Neo4j 初始化器。
 *
 * 启动时自动创建图谱唯一约束，标签只表达实体类型，项目归属统一通过 projectId 隔离。
 */
@Component
@Slf4j
public class NovelNeo4jInitializer {

    /**
     * 图谱约束语句——按方案 §4.2.0 的隔离原则。
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
            """.formatted(label(NovelGraphNodeEnum.CHAPTER), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NUMBER)),
            """
            CREATE CONSTRAINT novel_volume_project_number IF NOT EXISTS
            FOR (v:%s) REQUIRE (v.%s, v.%s) IS UNIQUE
            """.formatted(label(NovelGraphNodeEnum.VOLUME), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NUMBER)),
            """
            CREATE CONSTRAINT novel_item_project_name IF NOT EXISTS
            FOR (i:%s) REQUIRE (i.%s, i.%s) IS UNIQUE
            """.formatted(label(NovelGraphNodeEnum.ITEM), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME)),
            """
            CREATE CONSTRAINT novel_event_project_name IF NOT EXISTS
            FOR (e:%s) REQUIRE (e.%s, e.%s) IS UNIQUE
            """.formatted(label(NovelGraphNodeEnum.EVENT), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME)),
            """
            CREATE CONSTRAINT novel_cheat_project_name IF NOT EXISTS
            FOR (c:%s) REQUIRE (c.%s, c.%s) IS UNIQUE
            """.formatted(label(NovelGraphNodeEnum.CHEAT), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME)),
            """
            CREATE CONSTRAINT novel_alias_project_name IF NOT EXISTS
            FOR (a:%s) REQUIRE (a.%s, a.%s) IS UNIQUE
            """.formatted(label(NovelGraphNodeEnum.ALIAS), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME)),
            """
            CREATE CONSTRAINT novel_rule_project_name IF NOT EXISTS
            FOR (r:%s) REQUIRE (r.%s, r.%s) IS UNIQUE
            """.formatted(label(NovelGraphNodeEnum.NARRATIVE_RULE), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME))
    );

    @Resource
    private Driver novelNeo4jDriver;

    @Resource
    private NovelNeo4jProperties properties;

    /**
     * 初始化图谱约束。
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
            log.info("Neo4j 图谱约束初始化完成");
        } catch (Exception e) {
            log.warn("Neo4j 图谱约束初始化失败，请检查 Neo4j 服务和账号密码配置：{}", e.getMessage(), e);
        }
    }

    private static String label(NovelGraphNodeEnum nodeEnum) {
        return nodeEnum.label();
    }

    private static String property(NovelGraphPropertyEnum propertyEnum) {
        return propertyEnum.key();
    }
}
