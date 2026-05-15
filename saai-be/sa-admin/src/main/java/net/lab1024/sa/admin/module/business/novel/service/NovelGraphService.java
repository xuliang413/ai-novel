package net.lab1024.sa.admin.module.business.novel.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphNodeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphPropertyEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphRelationEnum;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelAliasEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCheatEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelChapterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelEventEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelItemEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelNarrativeRuleEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelVolumeEntity;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchOperationModel;
import org.apache.commons.collections4.CollectionUtils;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionContext;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Service;

/**
 * 小说知识图谱服务。
 *
 * 把 MySQL 中的项目、角色、地点、线索、章节同步到 Neo4j 中。
 * 图谱标签、关系类型、核心属性名都从枚举里取，避免拼错。
 *
 * 关系（按方案 §4.3）：
 *   CONTAINS —— 项目包含实体（角色/地点/线索/章节，Volume 未建时直连）
 *   PREVIOUS —— 章节前后顺序链
 *   APPEARS_IN —— 实体在某章出场
 *   ADVANCES —— 章节推进某条线索
 *
 * GraphPatch 白名单执行器（方案 §5.2.6）：
 *   只接受 6 种操作，后端拼 Cypher 防止注入，所有 Cypher 带 projectId 做项目隔离。
 */
@Service
@Slf4j
public class NovelGraphService {

    @Resource
    private Driver novelNeo4jDriver;

    /**
     * 检查 Neo4j 是否可连接。
     */
    public void check() {
        try (Session session = novelNeo4jDriver.session()) {
            session.run("RETURN 1").consume();
        }
    }

    /**
     * 合并项目节点。
     *
     * 项目节点使用 projectId 作为唯一业务键，mysqlId 只用于追溯来源表主键。
     */
    public void mergeProject(NovelProjectEntity project) {
        String cypher = """
                MERGE (p:%s {%s: $projectId})
                ON CREATE SET p.%s = datetime(),
                              p.%s = false
                SET p.%s = $projectId,
                    p.%s = $projectName,
                    p.%s = $genre,
                    p.%s = $summary,
                    p.%s = $protagonist,
                    p.%s = $status,
                    p.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.PROJECT),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT),
                property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.MYSQL_ID),
                property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.GENRE),
                property(NovelGraphPropertyEnum.SUMMARY),
                property(NovelGraphPropertyEnum.PROTAGONIST),
                property(NovelGraphPropertyEnum.STATUS),
                property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        try (Session session = novelNeo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run(cypher, Values.parameters(
                        "projectId", project.getProjectId(),
                        "projectName", project.getProjectName(),
                        "genre", project.getGenre(),
                        "summary", project.getSummary(),
                        "protagonist", project.getProtagonist(),
                        "status", project.getStatus()
                ));
                return null;
            });
        }
    }

    /**
     * 合并角色节点，并建立项目拥有角色关系。
     */
    public void mergeCharacter(NovelCharacterEntity character) {
        String cypher = """
                MERGE (p:%s {%s: $projectId})
                ON CREATE SET p.%s = datetime(),
                              p.%s = false
                SET p.%s = datetime()
                MERGE (c:%s {%s: $projectId, %s: $characterName})
                ON CREATE SET c.%s = datetime(),
                              c.%s = false
                SET c.%s = $characterId,
                    c.%s = $roleType,
                    c.%s = $summary,
                    c.%s = $currentStatus,
                    c.%s = datetime()
                MERGE (p)-[r:%s {%s: $projectId}]->(c)
                ON CREATE SET r.%s = datetime()
                SET r.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.PROJECT),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT),
                property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                node(NovelGraphNodeEnum.CHARACTER),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT),
                property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.MYSQL_ID),
                property(NovelGraphPropertyEnum.ROLE),
                property(NovelGraphPropertyEnum.SUMMARY),
                property(NovelGraphPropertyEnum.STATUS),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                relation(NovelGraphRelationEnum.CONTAINS),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT),
                property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        try (Session session = novelNeo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run(cypher, Values.parameters(
                        "projectId", character.getProjectId(),
                        "characterId", character.getCharacterId(),
                        "characterName", character.getCharacterName(),
                        "roleType", character.getRoleType(),
                        "summary", character.getSummary(),
                        "currentStatus", character.getCurrentStatus()
                ));
                return null;
            });
        }
    }

    /**
     * 合并地点节点，并建立项目拥有地点关系。
     */
    public void mergeLocation(NovelLocationEntity location) {
        String cypher = """
                MERGE (p:%s {%s: $projectId})
                ON CREATE SET p.%s = datetime(),
                              p.%s = false
                SET p.%s = datetime()
                MERGE (l:%s {%s: $projectId, %s: $locationName})
                ON CREATE SET l.%s = datetime(),
                              l.%s = false
                SET l.%s = $locationId,
                    l.%s = $locationType,
                    l.%s = $summary,
                    l.%s = datetime()
                MERGE (p)-[r:%s {%s: $projectId}]->(l)
                ON CREATE SET r.%s = datetime()
                SET r.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.PROJECT),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT),
                property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                node(NovelGraphNodeEnum.LOCATION),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT),
                property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.MYSQL_ID),
                property(NovelGraphPropertyEnum.TYPE),
                property(NovelGraphPropertyEnum.SUMMARY),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                relation(NovelGraphRelationEnum.CONTAINS),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT),
                property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        try (Session session = novelNeo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run(cypher, Values.parameters(
                        "projectId", location.getProjectId(),
                        "locationId", location.getLocationId(),
                        "locationName", location.getLocationName(),
                        "locationType", location.getLocationType(),
                        "summary", location.getSummary()
                ));
                return null;
            });
        }
    }

    /**
     * 合并线索节点，并建立项目拥有线索关系。
     */
    public void mergeClue(NovelClueEntity clue) {
        String cypher = """
                MERGE (p:%s {%s: $projectId})
                ON CREATE SET p.%s = datetime(),
                              p.%s = false
                SET p.%s = datetime()
                MERGE (c:%s {%s: $projectId, %s: $clueName})
                ON CREATE SET c.%s = datetime(),
                              c.%s = false
                SET c.%s = $clueId,
                    c.%s = $clueType,
                    c.%s = $clueStatus,
                    c.%s = $summary,
                    c.%s = datetime()
                MERGE (p)-[r:%s {%s: $projectId}]->(c)
                ON CREATE SET r.%s = datetime()
                SET r.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.PROJECT),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT),
                property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                node(NovelGraphNodeEnum.CLUE),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT),
                property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.MYSQL_ID),
                property(NovelGraphPropertyEnum.TYPE),
                property(NovelGraphPropertyEnum.STATUS),
                property(NovelGraphPropertyEnum.SUMMARY),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                relation(NovelGraphRelationEnum.CONTAINS),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT),
                property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        try (Session session = novelNeo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run(cypher, Values.parameters(
                        "projectId", clue.getProjectId(),
                        "clueId", clue.getClueId(),
                        "clueName", clue.getClueName(),
                        "clueType", clue.getClueType(),
                        "clueStatus", clue.getClueStatus(),
                        "summary", clue.getSummary()
                ));
                return null;
            });
        }
    }

    /**
     * 合并卷节点，并建立 Project -> Volume 结构关系。
     */
    public void mergeVolume(NovelVolumeEntity volume) {
        String cypher = """
                MERGE (p:%s {%s: $projectId})
                ON CREATE SET p.%s = datetime(), p.%s = false
                SET p.%s = datetime()
                MERGE (v:%s {%s: $projectId, %s: $volumeNo})
                ON CREATE SET v.%s = datetime(), v.%s = false
                SET v.%s = $volumeId,
                    v.%s = $volumeTitle,
                    v.%s = $summary,
                    v.%s = datetime()
                MERGE (p)-[r:%s {%s: $projectId}]->(v)
                ON CREATE SET r.%s = datetime()
                SET r.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.PROJECT), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                node(NovelGraphNodeEnum.VOLUME), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NUMBER),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.MYSQL_ID), property(NovelGraphPropertyEnum.TITLE),
                property(NovelGraphPropertyEnum.SUMMARY), property(NovelGraphPropertyEnum.UPDATED_AT),
                relation(NovelGraphRelationEnum.CONTAINS), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        try (Session session = novelNeo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run(cypher, Values.parameters(
                        "projectId", volume.getProjectId(),
                        "volumeId", volume.getVolumeId(),
                        "volumeNo", volume.getVolumeNo(),
                        "volumeTitle", volume.getVolumeTitle(),
                        "summary", volume.getSummary()
                ));
                return null;
            });
        }
    }

    /**
     * 合并物品节点。
     */
    public void mergeItem(NovelItemEntity item) {
        mergeNamedAsset(NovelGraphNodeEnum.ITEM, item.getProjectId(), item.getItemId(), item.getItemName(),
                item.getItemType(), item.getItemStatus(), item.getSummary());
    }

    /**
     * 合并事件节点。
     */
    public void mergeEvent(NovelEventEntity event) {
        String cypher = """
                MERGE (p:%s {%s: $projectId})
                ON CREATE SET p.%s = datetime(), p.%s = false
                SET p.%s = datetime()
                MERGE (e:%s {%s: $projectId, %s: $eventName})
                ON CREATE SET e.%s = datetime(), e.%s = false
                SET e.%s = $eventId,
                    e.%s = $summary,
                    e.%s = $chapterOccurred,
                    e.%s = datetime()
                MERGE (p)-[r:%s {%s: $projectId}]->(e)
                ON CREATE SET r.%s = datetime()
                SET r.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.PROJECT), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                node(NovelGraphNodeEnum.EVENT), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.MYSQL_ID), property(NovelGraphPropertyEnum.SUMMARY),
                property(NovelGraphPropertyEnum.CHAPTER_OCCURRED), property(NovelGraphPropertyEnum.UPDATED_AT),
                relation(NovelGraphRelationEnum.CONTAINS), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        try (Session session = novelNeo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run(cypher, Values.parameters(
                        "projectId", event.getProjectId(),
                        "eventId", event.getEventId(),
                        "eventName", event.getEventName(),
                        "summary", event.getSummary(),
                        "chapterOccurred", event.getChapterOccurred()
                ));
                return null;
            });
        }
    }

    /**
     * 合并金手指节点。
     */
    public void mergeCheat(NovelCheatEntity cheat) {
        String cypher = """
                MERGE (p:%s {%s: $projectId})
                ON CREATE SET p.%s = datetime(), p.%s = false
                SET p.%s = datetime()
                MERGE (c:%s {%s: $projectId, %s: $cheatName})
                ON CREATE SET c.%s = datetime(), c.%s = false
                SET c.%s = $cheatId,
                    c.%s = $cheatType,
                    c.%s = $summary,
                    c.%s = $origin,
                    c.%s = $limitation,
                    c.%s = $evolution,
                    c.%s = datetime()
                MERGE (p)-[r:%s {%s: $projectId}]->(c)
                ON CREATE SET r.%s = datetime()
                SET r.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.PROJECT), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                node(NovelGraphNodeEnum.CHEAT), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.MYSQL_ID), property(NovelGraphPropertyEnum.TYPE),
                property(NovelGraphPropertyEnum.SUMMARY), property(NovelGraphPropertyEnum.ORIGIN),
                property(NovelGraphPropertyEnum.LIMITATION), property(NovelGraphPropertyEnum.EVOLUTION),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                relation(NovelGraphRelationEnum.CONTAINS), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        try (Session session = novelNeo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run(cypher, Values.parameters(
                        "projectId", cheat.getProjectId(),
                        "cheatId", cheat.getCheatId(),
                        "cheatName", cheat.getCheatName(),
                        "cheatType", cheat.getCheatType(),
                        "summary", cheat.getSummary(),
                        "origin", cheat.getOrigin(),
                        "limitation", cheat.getLimitation(),
                        "evolution", cheat.getEvolution()
                ));
                return null;
            });
        }
    }

    /**
     * 合并马甲节点。
     */
    public void mergeAlias(NovelAliasEntity alias) {
        String cypher = """
                MERGE (p:%s {%s: $projectId})
                ON CREATE SET p.%s = datetime(), p.%s = false
                SET p.%s = datetime()
                MERGE (a:%s {%s: $projectId, %s: $aliasName})
                ON CREATE SET a.%s = datetime(), a.%s = false
                SET a.%s = $aliasId,
                    a.%s = $aliasType,
                    a.%s = $aliasContext,
                    a.%s = $summary,
                    a.%s = $revealed,
                    a.%s = $revealedTo,
                    a.%s = datetime()
                MERGE (p)-[r:%s {%s: $projectId}]->(a)
                ON CREATE SET r.%s = datetime()
                SET r.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.PROJECT), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                node(NovelGraphNodeEnum.ALIAS), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.MYSQL_ID), property(NovelGraphPropertyEnum.TYPE),
                property(NovelGraphPropertyEnum.CONTEXT), property(NovelGraphPropertyEnum.SUMMARY),
                property(NovelGraphPropertyEnum.REVEALED), property(NovelGraphPropertyEnum.REVEALED_TO),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                relation(NovelGraphRelationEnum.CONTAINS), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        try (Session session = novelNeo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run(cypher, Values.parameters(
                        "projectId", alias.getProjectId(),
                        "aliasId", alias.getAliasId(),
                        "aliasName", alias.getAliasName(),
                        "aliasType", alias.getAliasType(),
                        "aliasContext", alias.getAliasContext(),
                        "summary", alias.getSummary(),
                        "revealed", alias.getRevealed(),
                        "revealedTo", alias.getRevealedTo()
                ));
                return null;
            });
        }
    }

    /**
     * 合并叙事规则节点，并建立 HAS_RULE 关系。
     */
    public void mergeNarrativeRule(NovelNarrativeRuleEntity rule) {
        String cypher = """
                MERGE (p:%s {%s: $projectId})
                ON CREATE SET p.%s = datetime(), p.%s = false
                SET p.%s = datetime()
                MERGE (r:%s {%s: $projectId, %s: $ruleName})
                ON CREATE SET r.%s = datetime(), r.%s = false
                SET r.%s = $ruleId,
                    r.%s = $ruleType,
                    r.%s = $ruleValue,
                    r.%s = $priority,
                    r.%s = datetime()
                MERGE (p)-[rel:%s {%s: $projectId}]->(r)
                ON CREATE SET rel.%s = datetime()
                SET rel.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.PROJECT), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                node(NovelGraphNodeEnum.NARRATIVE_RULE), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.MYSQL_ID), property(NovelGraphPropertyEnum.TYPE),
                property(NovelGraphPropertyEnum.VALUE), property(NovelGraphPropertyEnum.PRIORITY),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                relation(NovelGraphRelationEnum.HAS_RULE), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        try (Session session = novelNeo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run(cypher, Values.parameters(
                        "projectId", rule.getProjectId(),
                        "ruleId", rule.getRuleId(),
                        "ruleName", rule.getRuleName(),
                        "ruleType", rule.getRuleType(),
                        "ruleValue", rule.getRuleValue(),
                        "priority", rule.getPriority()
                ));
                return null;
            });
        }
    }

    /**
     * 合并章节节点，并建立项目拥有章节关系。
     *
     * 章节节点不保存正文，只保存序号、标题、摘要和状态；正文仍以 MySQL 为准。
     */
    public void mergeChapter(NovelChapterEntity chapter) {
        String cypher = """
                MERGE (p:%s {%s: $projectId})
                ON CREATE SET p.%s = datetime(),
                              p.%s = false
                SET p.%s = datetime()
                MERGE (c:%s {%s: $projectId, %s: $chapterNo})
                ON CREATE SET c.%s = datetime(),
                              c.%s = false
                SET c.%s = $chapterId,
                    c.%s = $title,
                    c.%s = $summary,
                    c.%s = $status,
                    c.%s = datetime()
                MERGE (p)-[r:%s {%s: $projectId}]->(c)
                ON CREATE SET r.%s = datetime()
                SET r.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.PROJECT),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT),
                property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                node(NovelGraphNodeEnum.CHAPTER),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.NUMBER),
                property(NovelGraphPropertyEnum.CREATED_AT),
                property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.MYSQL_ID),
                property(NovelGraphPropertyEnum.TITLE),
                property(NovelGraphPropertyEnum.SUMMARY),
                property(NovelGraphPropertyEnum.STATUS),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                relation(NovelGraphRelationEnum.CONTAINS),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT),
                property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        try (Session session = novelNeo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run(cypher, Values.parameters(
                        "projectId", chapter.getProjectId(),
                        "chapterId", chapter.getChapterId(),
                        "chapterNo", chapter.getChapterNo(),
                        "title", chapter.getTitle(),
                        "summary", chapter.getSummary(),
                        "status", chapter.getStatus()
                ));
                // 建立 PREVIOUS 关系：上一章 → 这一章
                if (chapter.getChapterNo() != null && chapter.getChapterNo() > 1) {
                    linkPrevious(tx, chapter.getProjectId(), chapter.getChapterNo());
                }
                return null;
            });
        }
    }

    /**
     * 建立 PREVIOUS 关系：(第N-1章)-[:PREVIOUS]->(第N章)。
     *
     * 如果第 N-1 章还不存在（比如先写了第 3 章再补第 2 章），这条关系暂时缺失，等补写时自然补上。
     */
    private void linkPrevious(TransactionContext tx, Long projectId, Integer chapterNo) {
        tx.run("""
                MATCH (prev:Chapter {projectId: $projectId, number: $prevNo})
                MATCH (cur:Chapter {projectId: $projectId, number: $curNo})
                MERGE (prev)-[:PREVIOUS {projectId: $projectId}]->(cur)
                """,
                Values.parameters("projectId", projectId, "prevNo", chapterNo - 1, "curNo", chapterNo));
    }

    /**
     * 写入前先查一遍 Neo4j，看 patch 里声称的改前值跟图谱里实际的一不一致。
     *
     * 查到不一致就标成 CONFLICT，让用户自己决定。
     * 比如用户在变更确认页停了很久，中间另一个人改了图谱，就能检测出来。
     */
    public void validatePatch(NovelGraphPatchModel graphPatch) {
        if (graphPatch == null || CollectionUtils.isEmpty(graphPatch.getOperations())) {
            return;
        }
        try (Session session = novelNeo4jDriver.session()) {
            for (NovelGraphPatchOperationModel op : graphPatch.getOperations()) {
                if (!"READY".equals(op.getValidationStatus())) {
                    continue;
                }
                if ("ADVANCE_CLUE".equals(op.getOperationType())) {
                    checkClueBefore(session, graphPatch.getProjectId(), op);
                } else if ("UPDATE_CHAPTER_SUMMARY".equals(op.getOperationType())) {
                    checkChapterBefore(session, graphPatch.getProjectId(), graphPatch.getChapterNo(), op);
                }
                // MARK_APPEARANCE 没有 before 值要对比（第一次出场不会有冲突）
            }
        }
    }

    private void checkClueBefore(Session session, Long projectId, NovelGraphPatchOperationModel op) {
        var result = session.run(
                "MATCH (c:Clue {projectId: $pid, name: $name}) RETURN c.status AS status, c.summary AS summary",
                Values.parameters("pid", projectId, "name", op.getTargetName()));
        if (result.hasNext()) {
            var rec = result.single();
            String currentStatus = rec.get("status", (String) null);
            String beforeStatus = op.getBeforeStatus();
            if (beforeStatus != null && !beforeStatus.equals(currentStatus)) {
                op.setValidationStatus("CONFLICT");
                op.setReason("线索 " + op.getTargetName() + " 的状态已经不是 " + beforeStatus + "，当前是 " + currentStatus);
            }
        }
    }

    private void checkChapterBefore(Session session, Long projectId, Integer chapterNo, NovelGraphPatchOperationModel op) {
        var result = session.run(
                "MATCH (ch:Chapter {projectId: $pid, number: $no}) RETURN ch.summary AS summary",
                Values.parameters("pid", projectId, "no", chapterNo));
        if (result.hasNext()) {
            var rec = result.single();
            String currentSummary = rec.get("summary", (String) null);
            String beforeSummary = op.getBeforeSummary();
            if (beforeSummary != null && currentSummary != null && !beforeSummary.equals(currentSummary)) {
                op.setValidationStatus("CONFLICT");
                op.setReason("章节摘要已在图谱中发生变化，可能被其他操作改过");
            }
        }
    }

    /**
     * 检查必填字段——缺了关键字段的操作标成 BLOCKED，不让执行。
     *
     * 这里不是做所有业务判断，只做“没有这些字段肯定不能动图谱”的底线检查。
     * 例如移动角色必须知道角色名和新地点；新增节点必须知道节点类型和名称。
     *
     * 如果后面排查时发现某条操作没执行，先看 validationStatus：
     * BLOCKED 通常就是这里拦下来的，reason 会告诉你缺了什么。
     */
    public void checkBlocked(NovelGraphPatchModel graphPatch) {
        if (graphPatch == null || CollectionUtils.isEmpty(graphPatch.getOperations())) {
            return;
        }
        for (NovelGraphPatchOperationModel op : graphPatch.getOperations()) {
            // 前面流程已经判死刑的操作，不在这里重复改原因，保留第一现场。
            if ("BLOCKED".equals(op.getValidationStatus())) {
                continue;
            }
            // operationType 是白名单分发的钥匙，没有它，后面根本不知道该走哪条受控 Cypher。
            if (op.getOperationType() == null) {
                op.setValidationStatus("BLOCKED");
                op.setReason("缺少 operationType");
                continue;
            }
            // 出场关系必须明确“谁出场”。targetType 决定标签，targetName 决定节点。
            if ("MARK_APPEARANCE".equals(op.getOperationType()) || "UNMARK_APPEARANCE".equals(op.getOperationType())) {
                if (op.getTargetName() == null || op.getTargetType() == null) {
                    op.setValidationStatus("BLOCKED");
                    op.setReason("出场操作缺少 targetName 或 targetType");
                }
            }
            // CREATE/UPDATE 只允许改一个明确的业务节点，不能让一条模糊操作落进图谱。
            if (op.getOperationType().startsWith("CREATE_") || op.getOperationType().startsWith("UPDATE_")) {
                if (op.getTargetName() == null || op.getTargetType() == null) {
                    op.setValidationStatus("BLOCKED");
                    op.setReason("创建或更新操作缺少 targetName 或 targetType");
                }
            }
            // 线索推进一定要知道是哪条线索；状态和摘要可以为空，但线索名不能空。
            if ("ADVANCE_CLUE".equals(op.getOperationType()) || "RESTORE_CLUE".equals(op.getOperationType())) {
                if (op.getTargetName() == null) {
                    op.setValidationStatus("BLOCKED");
                    op.setReason("线索操作缺少 targetName");
                }
            }
            // 这些都是“从 A 指向 B”的关系操作。targetName 是 A，toName 是 B。
            if (("MOVE_CHARACTER".equals(op.getOperationType())
                    || "TRANSFER_ITEM".equals(op.getOperationType())
                    || "ASSIGN_CHEAT_TO_CHARACTER".equals(op.getOperationType())
                    || "BIND_CHEAT_TO_ITEM".equals(op.getOperationType())
                    || "ASSIGN_ALIAS_TO_CHARACTER".equals(op.getOperationType())
                    || "REVEAL_ALIAS_TO_CHARACTER".equals(op.getOperationType()))
                    && (op.getTargetName() == null || op.getToName() == null)) {
                op.setValidationStatus("BLOCKED");
                op.setReason("关系操作缺少 targetName 或 toName");
            }
        }
    }

    /**
     * 真正往 Neo4j 里面写数据。只执行校验过的操作，被标了 BLOCKED 和 CONFLICT 的跳过。
     *
     * 注意：这里不会替用户“聪明地修复”冲突。冲突代表当前图谱状态和变更单认知不一致，
     * 继续硬写容易污染设定，所以只跳过，留给前端和用户处理。
     */
    public void applyGraphPatch(NovelGraphPatchModel graphPatch) {
        if (graphPatch == null || CollectionUtils.isEmpty(graphPatch.getOperations())) {
            return;
        }

        try (Session session = novelNeo4jDriver.session()) {
            session.executeWrite(tx -> {
                for (NovelGraphPatchOperationModel operation : graphPatch.getOperations()) {
                    // BLOCKED 是字段不够或非法；CONFLICT 是图谱已变化。两种都不适合偷偷执行。
                    if ("BLOCKED".equals(operation.getValidationStatus())
                            || "CONFLICT".equals(operation.getValidationStatus())) {
                        continue;
                    }
                    executePatchOperation(tx, graphPatch, operation);
                }
                return null;
            });
        }
    }

    /**
     * 白名单分发：根据 operationType 路由到对应的 Cypher 执行方法。
     *
     * 这是 GraphPatch 安全闸门里最重要的一层。
     *
     * 业务限制：
     * 1. 不接受 RAW_CYPHER，也不执行 LLM 拼出来的任意语句。
     * 2. 每一种 operationType 必须在这里显式登记，没登记就抛错。
     * 3. 所有下游 Cypher 都必须带 projectId，避免串到别的小说项目。
     * 4. 这里只处理“业务语义动作”，例如“角色移动”“物品转移”“线索推进”，
     *    不给前端或 LLM 暴露底层节点/关系随便改的能力。
     *
     * 排查建议：如果某条变更确认时报“不支持的 GraphPatch 操作”，
     * 先来这里看 operationType 是否真的被加入白名单。
     */
    private void executePatchOperation(TransactionContext tx, NovelGraphPatchModel graphPatch, NovelGraphPatchOperationModel operation) {
        String operationType = operation.getOperationType();

        // 章节摘要是发布章节的主线动作：正文过审后，Chapter 节点只同步摘要和状态，不写正文。
        if ("UPDATE_CHAPTER_SUMMARY".equals(operationType) || "RESTORE_CHAPTER_SUMMARY".equals(operationType)) {
            updateChapterSummary(tx, graphPatch, operation);
            return;
        }

        // 出场记录只建立 APPEARS_IN，不改角色状态。它解决“第 N 章谁出现过”的检索问题。
        if ("MARK_APPEARANCE".equals(operationType)) {
            markAppearance(tx, graphPatch, operation);
            return;
        }
        // 这些是更具体的出场别名，最后都落到 APPEARS_IN，便于前端按按钮文案区分。
        if ("MARK_LOCATION_APPEARANCE".equals(operationType)
                || "MARK_ITEM_APPEARANCE".equals(operationType)
                || "MARK_EVENT_OCCURRED".equals(operationType)
                || "MARK_ALIAS_APPEARANCE".equals(operationType)) {
            markAppearance(tx, graphPatch, operation);
            return;
        }
        if ("UNMARK_APPEARANCE".equals(operationType)) {
            unmarkAppearance(tx, graphPatch, operation);
            return;
        }

        // CREATE_* 只创建白名单节点；节点类型由 targetType 决定，不从外部拼标签。
        if (operationType.startsWith("CREATE_")) {
            createOrUpdateNode(tx, graphPatch, operation);
            return;
        }
        // 撤销新增节点时不物理删除，而是 archived=true。这样误删还能找回来。
        if ("ARCHIVE_NODE".equals(operationType)) {
            archiveNode(tx, graphPatch, operation);
            return;
        }
        // 常规更新只改状态、摘要、类型、规则值这几个安全字段，不开放任意属性写入。
        if ("UPDATE_CHARACTER_STATE".equals(operationType)
                || "MARK_CHARACTER_STATUS".equals(operationType)
                || "UPDATE_LOCATION".equals(operationType)
                || "UPDATE_ITEM_STATUS".equals(operationType)
                || "UPDATE_EVENT".equals(operationType)
                || "UPDATE_CHEAT".equals(operationType)
                || "UPDATE_ALIAS".equals(operationType)
                || "UPDATE_VOLUME".equals(operationType)
                || "UPDATE_CLUE".equals(operationType)
                || "RESOLVE_CLUE".equals(operationType)
                || "ATTACH_RULE".equals(operationType)) {
            createOrUpdateNode(tx, graphPatch, operation);
            return;
        }
        // 人物关系风险较高，尤其 LOVES/HATES/IS_FAMILY_OF。这里只执行用户确认后的关系类型。
        if ("UPDATE_CHARACTER_RELATION".equals(operationType)) {
            linkNamedNodes(tx, graphPatch, operation, NovelGraphNodeEnum.CHARACTER, NovelGraphNodeEnum.CHARACTER, characterRelation(operation));
            return;
        }
        // 事件参与、线索牵连、知情关系都是“建立关系”，不改两端节点本身。
        if ("LINK_EVENT_PARTICIPANT".equals(operationType)) {
            linkNamedNodes(tx, graphPatch, operation, NovelGraphNodeEnum.CHARACTER, NovelGraphNodeEnum.EVENT, NovelGraphRelationEnum.PARTICIPATES_IN);
            return;
        }
        if ("LINK_CLUE_CHARACTER".equals(operationType)) {
            linkNamedNodes(tx, graphPatch, operation, NovelGraphNodeEnum.CLUE, NovelGraphNodeEnum.CHARACTER, NovelGraphRelationEnum.INVOLVES);
            return;
        }
        if ("MARK_CHARACTER_KNOWS_CLUE".equals(operationType)) {
            linkNamedNodes(tx, graphPatch, operation, NovelGraphNodeEnum.CHARACTER, NovelGraphNodeEnum.CLUE, NovelGraphRelationEnum.KNOWS_ABOUT);
            return;
        }
        if ("INTERSECT_CLUES".equals(operationType)) {
            linkNamedNodes(tx, graphPatch, operation, NovelGraphNodeEnum.CLUE, NovelGraphNodeEnum.CLUE, NovelGraphRelationEnum.INTERSECTS);
            return;
        }
        if ("TRIGGER_CLUE".equals(operationType)) {
            linkNamedNodes(tx, graphPatch, operation, NovelGraphNodeEnum.EVENT, NovelGraphNodeEnum.CLUE, NovelGraphRelationEnum.TRIGGERS);
            return;
        }
        if ("ASSIGN_CLUE_TO_VOLUME".equals(operationType)) {
            linkNamedNodes(tx, graphPatch, operation, NovelGraphNodeEnum.CLUE, NovelGraphNodeEnum.VOLUME, NovelGraphRelationEnum.BELONGS_TO);
            return;
        }
        // 角色当前位置是单一事实源：移动时会删除旧 CURRENTLY_AT，再建立新关系。
        if ("MOVE_CHARACTER".equals(operationType)) {
            moveCharacter(tx, graphPatch, operation);
            return;
        }
        // 物品持有人同样是单一事实源：转移时会清掉旧 POSSESSES。
        if ("TRANSFER_ITEM".equals(operationType)) {
            transferItem(tx, graphPatch, operation);
            return;
        }
        // 金手指/马甲属于角色资产。这里用关系表达归属，节点本身仍可被项目管理页维护。
        if ("ASSIGN_CHEAT_TO_CHARACTER".equals(operationType)) {
            linkCharacterToAsset(tx, graphPatch, operation, NovelGraphNodeEnum.CHEAT, NovelGraphRelationEnum.HAS_CHEAT);
            return;
        }
        if ("BIND_CHEAT_TO_ITEM".equals(operationType)) {
            linkAssetToAsset(tx, graphPatch, operation, NovelGraphNodeEnum.CHEAT, NovelGraphNodeEnum.ITEM, NovelGraphRelationEnum.BOUND_TO);
            return;
        }
        if ("ASSIGN_ALIAS_TO_CHARACTER".equals(operationType)) {
            linkCharacterToAsset(tx, graphPatch, operation, NovelGraphNodeEnum.ALIAS, NovelGraphRelationEnum.HAS_ALIAS);
            return;
        }
        if ("REVEAL_ALIAS_TO_CHARACTER".equals(operationType)) {
            linkCharacterToAsset(tx, graphPatch, operation, NovelGraphNodeEnum.ALIAS, NovelGraphRelationEnum.KNOWS_ALIAS);
            return;
        }
        // 线索推进会更新线索当前摘要/状态，并记录“哪一章推进了它”。
        if ("ADVANCE_CLUE".equals(operationType)) {
            advanceClue(tx, graphPatch, operation);
            return;
        }
        // 撤销线索推进时恢复旧摘要/状态，并删除本章 ADVANCES 关系。
        if ("RESTORE_CLUE".equals(operationType)) {
            restoreClue(tx, graphPatch, operation);
            return;
        }
        throw new IllegalArgumentException("不支持的 GraphPatch 操作：" + operationType);
    }

    /**
     * 更新章节图节点的摘要和发布状态。
     *
     * 正文全文仍然只放 MySQL，Neo4j 的 Chapter 节点只存“检索够用的短摘要”。
     */
    private void updateChapterSummary(TransactionContext tx, NovelGraphPatchModel graphPatch, NovelGraphPatchOperationModel operation) {
        String cypher = """
                MERGE (c:%s {%s: $projectId, %s: $chapterNo})
                ON CREATE SET c.%s = datetime(),
                              c.%s = false,
                              c.%s = $patchId
                SET c.%s = $summary,
                    c.%s = $status,
                    c.%s = $patchId,
                    c.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.CHAPTER),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.NUMBER),
                property(NovelGraphPropertyEnum.CREATED_AT),
                property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.SUMMARY),
                property(NovelGraphPropertyEnum.STATUS),
                property(NovelGraphPropertyEnum.UPDATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        tx.run(cypher, Values.parameters(
                "projectId", graphPatch.getProjectId(),
                "chapterNo", graphPatch.getChapterNo(),
                "summary", operation.getAfterSummary(),
                "status", operation.getAfterStatus(),
                "patchId", graphPatch.getPatchId()
        ));
    }

    /**
     * 记录某个实体在本章出现。
     *
     * 业务限制：这里只接受已能定位到标签和名称的实体，不负责凭空创建复杂设定。
     * 如果 AI 提到“国师”但系统里没有这个角色，应该先走 CREATE_CHARACTER，再走 MARK_APPEARANCE。
     */
    private void markAppearance(TransactionContext tx, NovelGraphPatchModel graphPatch, NovelGraphPatchOperationModel operation) {
        String targetLabel = targetLabel(operation.getTargetType());
        String cypher = """
                MATCH (target:%s {%s: $projectId, %s: $targetName})
                MATCH (chapter:%s {%s: $projectId, %s: $chapterNo})
                MERGE (target)-[r:%s {%s: $projectId, %s: $chapterNo}]->(chapter)
                ON CREATE SET r.%s = datetime(),
                              r.%s = $patchId
                SET r.%s = $patchId,
                    r.%s = datetime()
                """.formatted(
                targetLabel,
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.NAME),
                node(NovelGraphNodeEnum.CHAPTER),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.NUMBER),
                relation(NovelGraphRelationEnum.APPEARS_IN),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CHAPTER_NO),
                property(NovelGraphPropertyEnum.CREATED_AT),
                property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.UPDATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        tx.run(cypher, Values.parameters(
                "projectId", graphPatch.getProjectId(),
                "chapterNo", graphPatch.getChapterNo(),
                "targetName", operation.getTargetName(),
                "patchId", graphPatch.getPatchId()
        ));
    }

    /**
     * 撤销本章出场记录。
     *
     * 它只删除 APPEARS_IN 关系，不删除角色/地点/物品本身。
     */
    private void unmarkAppearance(TransactionContext tx, NovelGraphPatchModel graphPatch, NovelGraphPatchOperationModel operation) {
        String targetLabel = targetLabel(operation.getTargetType());
        String cypher = """
                MATCH (target:%s {%s: $projectId, %s: $targetName})-[r:%s {%s: $projectId, %s: $chapterNo}]->(chapter:%s {%s: $projectId, %s: $chapterNo})
                DELETE r
                """.formatted(
                targetLabel,
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.NAME),
                relation(NovelGraphRelationEnum.APPEARS_IN),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CHAPTER_NO),
                node(NovelGraphNodeEnum.CHAPTER),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.NUMBER)
        );

        tx.run(cypher, Values.parameters(
                "projectId", graphPatch.getProjectId(),
                "chapterNo", graphPatch.getChapterNo(),
                "targetName", operation.getTargetName()
        ));
    }

    /**
     * 推进线索。
     *
     * 这里会做两件事：
     * 1. 更新 Clue 节点当前状态/摘要；
     * 2. 建立 Chapter -ADVANCES-> Clue，留下“是哪一章推动了它”的证据。
     */
    private void advanceClue(TransactionContext tx, NovelGraphPatchModel graphPatch, NovelGraphPatchOperationModel operation) {
        String cypher = """
                MATCH (chapter:%s {%s: $projectId, %s: $chapterNo})
                MATCH (clue:%s {%s: $projectId, %s: $targetName})
                SET clue.%s = $status,
                    clue.%s = $summary,
                    clue.%s = $patchId,
                    clue.%s = datetime()
                MERGE (chapter)-[r:%s {%s: $projectId, %s: $chapterNo}]->(clue)
                ON CREATE SET r.%s = datetime(),
                              r.%s = $patchId
                SET r.%s = $patchId,
                    r.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.CHAPTER),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.NUMBER),
                node(NovelGraphNodeEnum.CLUE),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.STATUS),
                property(NovelGraphPropertyEnum.SUMMARY),
                property(NovelGraphPropertyEnum.UPDATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                relation(NovelGraphRelationEnum.ADVANCES),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CHAPTER_NO),
                property(NovelGraphPropertyEnum.CREATED_AT),
                property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.UPDATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        tx.run(cypher, Values.parameters(
                "projectId", graphPatch.getProjectId(),
                "chapterNo", graphPatch.getChapterNo(),
                "targetName", operation.getTargetName(),
                "status", operation.getAfterStatus(),
                "summary", operation.getAfterSummary(),
                "patchId", graphPatch.getPatchId()
        ));
    }

    /**
     * 撤销线索推进。
     *
     * inversePatch 会把 beforeStatus / beforeSummary 交换到 after 字段，
     * 所以这里仍然读取 operation.after*，但语义上是在恢复旧值。
     */
    private void restoreClue(TransactionContext tx, NovelGraphPatchModel graphPatch, NovelGraphPatchOperationModel operation) {
        String cypher = """
                MATCH (chapter:%s {%s: $projectId, %s: $chapterNo})-[r:%s {%s: $projectId, %s: $chapterNo}]->(clue:%s {%s: $projectId, %s: $targetName})
                SET clue.%s = $status,
                    clue.%s = $summary,
                    clue.%s = $patchId,
                    clue.%s = datetime()
                DELETE r
                """.formatted(
                node(NovelGraphNodeEnum.CHAPTER),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.NUMBER),
                relation(NovelGraphRelationEnum.ADVANCES),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CHAPTER_NO),
                node(NovelGraphNodeEnum.CLUE),
                property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.STATUS),
                property(NovelGraphPropertyEnum.SUMMARY),
                property(NovelGraphPropertyEnum.UPDATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        tx.run(cypher, Values.parameters(
                "projectId", graphPatch.getProjectId(),
                "chapterNo", graphPatch.getChapterNo(),
                "targetName", operation.getTargetName(),
                "status", operation.getAfterStatus(),
                "summary", operation.getAfterSummary(),
                "patchId", graphPatch.getPatchId()
        ));
    }

    /**
     * 创建或更新一个白名单节点。
     *
     * 这是一个“保守通用入口”：只写 summary/status/type/value 这些安全字段。
     * 不在这里支持任意属性，是为了避免 LLM 或前端把图谱结构写乱。
     */
    private void createOrUpdateNode(TransactionContext tx, NovelGraphPatchModel graphPatch, NovelGraphPatchOperationModel operation) {
        String targetLabel = targetLabel(operation.getTargetType());
        // 叙事规则挂到 Project 用 HAS_RULE；其他资产暂时都作为项目内容用 CONTAINS。
        NovelGraphRelationEnum relationEnum = "RULE".equals(operation.getTargetType()) || "NARRATIVE_RULE".equals(operation.getTargetType())
                ? NovelGraphRelationEnum.HAS_RULE
                : NovelGraphRelationEnum.CONTAINS;
        String cypher = """
                MERGE (p:%s {%s: $projectId})
                ON CREATE SET p.%s = datetime(), p.%s = false
                SET p.%s = datetime()
                MERGE (target:%s {%s: $projectId, %s: $targetName})
                ON CREATE SET target.%s = datetime(),
                              target.%s = false,
                              target.%s = $patchId
                SET target.%s = coalesce($summary, target.%s),
                    target.%s = coalesce($status, target.%s),
                    target.%s = coalesce($type, target.%s),
                    target.%s = coalesce($value, target.%s),
                    target.%s = $patchId,
                    target.%s = datetime()
                MERGE (p)-[r:%s {%s: $projectId}]->(target)
                ON CREATE SET r.%s = datetime(),
                              r.%s = $patchId
                SET r.%s = $patchId,
                    r.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.PROJECT), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                targetLabel, property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.SUMMARY), property(NovelGraphPropertyEnum.SUMMARY),
                property(NovelGraphPropertyEnum.STATUS), property(NovelGraphPropertyEnum.STATUS),
                property(NovelGraphPropertyEnum.TYPE), property(NovelGraphPropertyEnum.TYPE),
                property(NovelGraphPropertyEnum.VALUE), property(NovelGraphPropertyEnum.VALUE),
                property(NovelGraphPropertyEnum.UPDATED_BY_PATCH_ID), property(NovelGraphPropertyEnum.UPDATED_AT),
                relation(relationEnum), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.UPDATED_BY_PATCH_ID), property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        tx.run(cypher, Values.parameters(
                "projectId", graphPatch.getProjectId(),
                "targetName", operation.getTargetName(),
                "summary", operation.getAfterSummary(),
                "status", operation.getAfterStatus(),
                "type", operation.getRelationType(),
                "value", operation.getAfterValue(),
                "patchId", graphPatch.getPatchId()
        ));
    }

    /**
     * 归档节点。
     *
     * 新增实体的撤销不做物理删除，因为后面排查时经常需要看到“它曾经被创建过”。
     * 查询默认过滤 archived=true，所以归档后不会再污染写作上下文。
     */
    private void archiveNode(TransactionContext tx, NovelGraphPatchModel graphPatch, NovelGraphPatchOperationModel operation) {
        String targetLabel = targetLabel(operation.getTargetType());
        String cypher = """
                MATCH (target:%s {%s: $projectId, %s: $targetName})
                SET target.%s = true,
                    target.%s = $patchId,
                    target.%s = datetime()
                """.formatted(
                targetLabel, property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.UPDATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        tx.run(cypher, Values.parameters(
                "projectId", graphPatch.getProjectId(),
                "targetName", operation.getTargetName(),
                "patchId", graphPatch.getPatchId()
        ));
    }

    /**
     * 移动角色当前位置。
     *
     * CURRENTLY_AT 是单一事实源，一个角色同一时间只应该有一个当前位置。
     * 所以这里先删除旧 CURRENTLY_AT，再建立新关系。
     */
    private void moveCharacter(TransactionContext tx, NovelGraphPatchModel graphPatch, NovelGraphPatchOperationModel operation) {
        String cypher = """
                MATCH (character:%s {%s: $projectId, %s: $characterName})
                MERGE (location:%s {%s: $projectId, %s: $locationName})
                ON CREATE SET location.%s = datetime(),
                              location.%s = false,
                              location.%s = $patchId
                SET location.%s = datetime()
                WITH character, location
                OPTIONAL MATCH (character)-[old:%s {%s: $projectId}]->(:%s {%s: $projectId})
                DELETE old
                MERGE (character)-[r:%s {%s: $projectId}]->(location)
                ON CREATE SET r.%s = datetime(),
                              r.%s = $patchId
                SET r.%s = $patchId,
                    r.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.CHARACTER), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                node(NovelGraphNodeEnum.LOCATION), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID), property(NovelGraphPropertyEnum.UPDATED_AT),
                relation(NovelGraphRelationEnum.CURRENTLY_AT), property(NovelGraphPropertyEnum.PROJECT_ID),
                node(NovelGraphNodeEnum.LOCATION), property(NovelGraphPropertyEnum.PROJECT_ID),
                relation(NovelGraphRelationEnum.CURRENTLY_AT), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.UPDATED_BY_PATCH_ID), property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        tx.run(cypher, Values.parameters(
                "projectId", graphPatch.getProjectId(),
                "characterName", operation.getTargetName(),
                "locationName", operation.getToName(),
                "patchId", graphPatch.getPatchId()
        ));
    }

    /**
     * 转移物品持有人。
     *
     * POSSESSES 也按单一事实源处理：一件物品默认只有一个当前持有人。
     * 如果后面要支持“多人共有”，需要新增专门的操作类型，不要复用这个。
     */
    private void transferItem(TransactionContext tx, NovelGraphPatchModel graphPatch, NovelGraphPatchOperationModel operation) {
        String cypher = """
                MERGE (character:%s {%s: $projectId, %s: $characterName})
                ON CREATE SET character.%s = datetime(), character.%s = false, character.%s = $patchId
                MERGE (item:%s {%s: $projectId, %s: $itemName})
                ON CREATE SET item.%s = datetime(), item.%s = false, item.%s = $patchId
                WITH character, item
                OPTIONAL MATCH (:Character {%s: $projectId})-[old:%s {%s: $projectId}]->(item)
                DELETE old
                MERGE (character)-[r:%s {%s: $projectId}]->(item)
                ON CREATE SET r.%s = datetime(), r.%s = $patchId
                SET r.%s = $patchId, r.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.CHARACTER), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED), property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                node(NovelGraphNodeEnum.ITEM), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED), property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.PROJECT_ID), relation(NovelGraphRelationEnum.POSSESSES), property(NovelGraphPropertyEnum.PROJECT_ID),
                relation(NovelGraphRelationEnum.POSSESSES), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.UPDATED_BY_PATCH_ID), property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        tx.run(cypher, Values.parameters(
                "projectId", graphPatch.getProjectId(),
                "itemName", operation.getTargetName(),
                "characterName", operation.getToName(),
                "patchId", graphPatch.getPatchId()
        ));
    }

    /**
     * 建立“角色拥有某类资产”的关系。
     *
     * 现在用于：
     * - Character -HAS_CHEAT-> Cheat
     * - Character -HAS_ALIAS-> Alias
     * - Character -KNOWS_ALIAS-> Alias
     *
     * 这里用 MERGE，是因为重复确认同一个关系不应该产生多条边。
     */
    private void linkCharacterToAsset(TransactionContext tx,
                                      NovelGraphPatchModel graphPatch,
                                      NovelGraphPatchOperationModel operation,
                                      NovelGraphNodeEnum assetNode,
                                      NovelGraphRelationEnum relationEnum) {
        String cypher = """
                MERGE (character:%s {%s: $projectId, %s: $characterName})
                ON CREATE SET character.%s = datetime(), character.%s = false, character.%s = $patchId
                MERGE (asset:%s {%s: $projectId, %s: $assetName})
                ON CREATE SET asset.%s = datetime(), asset.%s = false, asset.%s = $patchId
                MERGE (character)-[r:%s {%s: $projectId}]->(asset)
                ON CREATE SET r.%s = datetime(), r.%s = $patchId
                SET r.%s = $patchId, r.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.CHARACTER), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED), property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                node(assetNode), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED), property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                relation(relationEnum), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.UPDATED_BY_PATCH_ID), property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        tx.run(cypher, Values.parameters(
                "projectId", graphPatch.getProjectId(),
                "characterName", operation.getToName(),
                "assetName", operation.getTargetName(),
                "patchId", graphPatch.getPatchId()
        ));
    }

    /**
     * 建立资产和资产之间的关系。
     *
     * 目前用于 Cheat -BOUND_TO-> Item，比如“系统绑定在古玉上”。
     */
    private void linkAssetToAsset(TransactionContext tx,
                                  NovelGraphPatchModel graphPatch,
                                  NovelGraphPatchOperationModel operation,
                                  NovelGraphNodeEnum fromNode,
                                  NovelGraphNodeEnum toNode,
                                  NovelGraphRelationEnum relationEnum) {
        String cypher = """
                MERGE (fromNode:%s {%s: $projectId, %s: $fromName})
                ON CREATE SET fromNode.%s = datetime(), fromNode.%s = false, fromNode.%s = $patchId
                MERGE (toNode:%s {%s: $projectId, %s: $toName})
                ON CREATE SET toNode.%s = datetime(), toNode.%s = false, toNode.%s = $patchId
                MERGE (fromNode)-[r:%s {%s: $projectId}]->(toNode)
                ON CREATE SET r.%s = datetime(), r.%s = $patchId
                SET r.%s = $patchId, r.%s = datetime()
                """.formatted(
                node(fromNode), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED), property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                node(toNode), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED), property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                relation(relationEnum), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.UPDATED_BY_PATCH_ID), property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        tx.run(cypher, Values.parameters(
                "projectId", graphPatch.getProjectId(),
                "fromName", operation.getTargetName(),
                "toName", operation.getToName(),
                "patchId", graphPatch.getPatchId()
        ));
    }

    /**
     * 建立两个命名节点之间的普通关系。
     *
     * 这是一个通用工具，但仍然是白名单内部工具：调用者必须先指定 from/to 标签和关系类型。
     * 它不会接收外部传来的标签名或关系名，因此不会绕开白名单。
     */
    private void linkNamedNodes(TransactionContext tx,
                                NovelGraphPatchModel graphPatch,
                                NovelGraphPatchOperationModel operation,
                                NovelGraphNodeEnum fromNode,
                                NovelGraphNodeEnum toNode,
                                NovelGraphRelationEnum relationEnum) {
        String cypher = """
                MERGE (fromNode:%s {%s: $projectId, %s: $fromName})
                ON CREATE SET fromNode.%s = datetime(), fromNode.%s = false, fromNode.%s = $patchId
                MERGE (toNode:%s {%s: $projectId, %s: $toName})
                ON CREATE SET toNode.%s = datetime(), toNode.%s = false, toNode.%s = $patchId
                MERGE (fromNode)-[r:%s {%s: $projectId}]->(toNode)
                ON CREATE SET r.%s = datetime(), r.%s = $patchId
                SET r.%s = $patchId,
                    r.%s = datetime(),
                    r.%s = coalesce($relationType, r.%s),
                    r.%s = coalesce($value, r.%s)
                """.formatted(
                node(fromNode), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED), property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                node(toNode), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED), property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                relation(relationEnum), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.CREATED_BY_PATCH_ID),
                property(NovelGraphPropertyEnum.UPDATED_BY_PATCH_ID), property(NovelGraphPropertyEnum.UPDATED_AT),
                property(NovelGraphPropertyEnum.RELATION_TYPE), property(NovelGraphPropertyEnum.RELATION_TYPE),
                property(NovelGraphPropertyEnum.VALUE), property(NovelGraphPropertyEnum.VALUE)
        );

        tx.run(cypher, Values.parameters(
                "projectId", graphPatch.getProjectId(),
                "fromName", operation.getTargetName(),
                "toName", operation.getToName(),
                "relationType", operation.getRelationType(),
                "value", operation.getAfterValue(),
                "patchId", graphPatch.getPatchId()
        ));
    }

    /**
     * 把人物关系的 relationType 收敛到允许的几类关系。
     *
     * 这里故意兜底为 KNOWS：如果 AI 给了一个奇怪的关系类型，不直接创建新关系类型，
     * 防止图谱里冒出不可控的边。
     */
    private NovelGraphRelationEnum characterRelation(NovelGraphPatchOperationModel operation) {
        if ("LOVES".equals(operation.getRelationType())) {
            return NovelGraphRelationEnum.LOVES;
        }
        if ("HATES".equals(operation.getRelationType())) {
            return NovelGraphRelationEnum.HATES;
        }
        if ("IS_FAMILY_OF".equals(operation.getRelationType())) {
            return NovelGraphRelationEnum.IS_FAMILY_OF;
        }
        return NovelGraphRelationEnum.KNOWS;
    }

    /**
     * 同步“项目直接管理的命名资产”到 Neo4j。
     *
     * 物品、地点、线索这类资产都遵循同一个结构：
     * Project -CONTAINS-> Asset，Asset 用 projectId + name 唯一定位。
     */
    private void mergeNamedAsset(NovelGraphNodeEnum nodeEnum,
                                 Long projectId,
                                 Long mysqlId,
                                 String name,
                                 String type,
                                 String status,
                                 String summary) {
        String cypher = """
                MERGE (p:%s {%s: $projectId})
                ON CREATE SET p.%s = datetime(), p.%s = false
                SET p.%s = datetime()
                MERGE (a:%s {%s: $projectId, %s: $name})
                ON CREATE SET a.%s = datetime(), a.%s = false
                SET a.%s = $mysqlId,
                    a.%s = $type,
                    a.%s = $status,
                    a.%s = $summary,
                    a.%s = datetime()
                MERGE (p)-[r:%s {%s: $projectId}]->(a)
                ON CREATE SET r.%s = datetime()
                SET r.%s = datetime()
                """.formatted(
                node(NovelGraphNodeEnum.PROJECT), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                node(nodeEnum), property(NovelGraphPropertyEnum.PROJECT_ID), property(NovelGraphPropertyEnum.NAME),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.ARCHIVED),
                property(NovelGraphPropertyEnum.MYSQL_ID), property(NovelGraphPropertyEnum.TYPE),
                property(NovelGraphPropertyEnum.STATUS), property(NovelGraphPropertyEnum.SUMMARY),
                property(NovelGraphPropertyEnum.UPDATED_AT),
                relation(NovelGraphRelationEnum.CONTAINS), property(NovelGraphPropertyEnum.PROJECT_ID),
                property(NovelGraphPropertyEnum.CREATED_AT), property(NovelGraphPropertyEnum.UPDATED_AT)
        );

        try (Session session = novelNeo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run(cypher, Values.parameters(
                        "projectId", projectId,
                        "mysqlId", mysqlId,
                        "name", name,
                        "type", type,
                        "status", status,
                        "summary", summary
                ));
                return null;
            });
        }
    }

    /**
     * 将 GraphPatch 的 targetType 翻译成 Neo4j 标签。
     *
     * 注意：Neo4j 标签不能参数化，所以所有可用标签必须在这里列死。
     * 如果以后新增节点类型，先补枚举，再补这里，最后再补白名单操作。
     */
    private String targetLabel(String targetType) {
        if ("CHARACTER".equals(targetType)) {
            return node(NovelGraphNodeEnum.CHARACTER);
        }
        if ("LOCATION".equals(targetType)) {
            return node(NovelGraphNodeEnum.LOCATION);
        }
        if ("CLUE".equals(targetType)) {
            return node(NovelGraphNodeEnum.CLUE);
        }
        if ("ITEM".equals(targetType)) {
            return node(NovelGraphNodeEnum.ITEM);
        }
        if ("EVENT".equals(targetType)) {
            return node(NovelGraphNodeEnum.EVENT);
        }
        if ("CHEAT".equals(targetType)) {
            return node(NovelGraphNodeEnum.CHEAT);
        }
        if ("ALIAS".equals(targetType)) {
            return node(NovelGraphNodeEnum.ALIAS);
        }
        if ("RULE".equals(targetType) || "NARRATIVE_RULE".equals(targetType)) {
            return node(NovelGraphNodeEnum.NARRATIVE_RULE);
        }
        if ("VOLUME".equals(targetType)) {
            return node(NovelGraphNodeEnum.VOLUME);
        }
        throw new IllegalArgumentException("不支持的 GraphPatch 目标类型：" + targetType);
    }

    private String node(NovelGraphNodeEnum nodeEnum) {
        return nodeEnum.label();
    }

    private String relation(NovelGraphRelationEnum relationEnum) {
        return relationEnum.type();
    }

    private String property(NovelGraphPropertyEnum propertyEnum) {
        return propertyEnum.key();
    }
}
