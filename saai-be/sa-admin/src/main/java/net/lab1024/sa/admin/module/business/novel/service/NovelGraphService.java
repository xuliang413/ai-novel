package net.lab1024.sa.admin.module.business.novel.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphNodeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphPropertyEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphRelationEnum;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelChapterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
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
 * M0 只做最小图谱写入：Project 以及其下的 Character、Location、Clue、Chapter。
 * 图谱标签、关系类型、核心属性名都从枚举取值，避免后续扩展时出现拼写不一致。
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
                relation(NovelGraphRelationEnum.HAS_CHARACTER),
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
                relation(NovelGraphRelationEnum.HAS_LOCATION),
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
                relation(NovelGraphRelationEnum.HAS_CLUE),
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
                relation(NovelGraphRelationEnum.HAS_CHAPTER),
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
                return null;
            });
        }
    }

    /**
     * 执行业务语义型 GraphPatch。
     *
     * 这里是 M1 图谱写入的唯一入口：只接受白名单 operationType，并由后端拼装受控 Cypher。
     */
    public void applyGraphPatch(NovelGraphPatchModel graphPatch) {
        if (graphPatch == null || CollectionUtils.isEmpty(graphPatch.getOperations())) {
            return;
        }

        try (Session session = novelNeo4jDriver.session()) {
            session.executeWrite(tx -> {
                for (NovelGraphPatchOperationModel operation : graphPatch.getOperations()) {
                    executePatchOperation(tx, graphPatch, operation);
                }
                return null;
            });
        }
    }

    private void executePatchOperation(TransactionContext tx, NovelGraphPatchModel graphPatch, NovelGraphPatchOperationModel operation) {
        String operationType = operation.getOperationType();
        if ("UPDATE_CHAPTER_SUMMARY".equals(operationType) || "RESTORE_CHAPTER_SUMMARY".equals(operationType)) {
            updateChapterSummary(tx, graphPatch, operation);
            return;
        }
        if ("MARK_APPEARANCE".equals(operationType)) {
            markAppearance(tx, graphPatch, operation);
            return;
        }
        if ("UNMARK_APPEARANCE".equals(operationType)) {
            unmarkAppearance(tx, graphPatch, operation);
            return;
        }
        if ("ADVANCE_CLUE".equals(operationType)) {
            advanceClue(tx, graphPatch, operation);
            return;
        }
        if ("RESTORE_CLUE".equals(operationType)) {
            restoreClue(tx, graphPatch, operation);
            return;
        }
        throw new IllegalArgumentException("不支持的 GraphPatch 操作：" + operationType);
    }

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
