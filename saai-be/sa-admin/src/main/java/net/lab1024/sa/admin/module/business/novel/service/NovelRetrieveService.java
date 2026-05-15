package net.lab1024.sa.admin.module.business.novel.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.model.ChapterIntentCandidateModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.ChapterIntentModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 从 Neo4j 图谱里查东西——写作前要知道的上下文。
 *
 * 查什么：
 * - 前一章写了什么（要衔接上）
 * - 候选角色现在什么状态、在哪、情绪怎样
 * - 角色之间的恩怨情仇（只给能推剧情的，普通认识就不给了）
 * - 目标线索现在推理到哪一步了
 * - POV 角色现在在哪
 *
 * 所有查询都带 projectId，不会串项目。
 */
@Slf4j
@Service
public class NovelRetrieveService {

    @Resource
    private Driver novelNeo4jDriver;

    public static final int TOKEN_BUDGET = 6000;
    public static final int HARD_LIMIT = 8000;

    /**
     * 一次性查出所有需要的上下文，调不同的 Cypher 查不同的东西。
     */
    public RetrievalResult retrieve(NovelProjectEntity project, ChapterIntentModel intent) {
        List<String> povCandidates = intent.getCandidateCharacters().stream()
                .filter(ChapterIntentCandidateModel::getRequired)
                .map(ChapterIntentCandidateModel::getName)
                .collect(Collectors.toList());
        String pov = StringUtils.defaultIfBlank(intent.getPov(),
                povCandidates.isEmpty() ? null : povCandidates.get(0));

        RetrievalResult result = new RetrievalResult();
        result.projectId = project.getProjectId();
        result.chapterNo = intent.getChapterNo();
        result.pov = pov;

        try (Session session = novelNeo4jDriver.session()) {
            result.previousChapterSummary = fetchPreviousChapterSummary(session, project.getProjectId(), intent.getChapterNo());
            result.chapterSummaries = fetchRecentChapterSummaries(session, project.getProjectId(), intent.getChapterNo(), 3);

            for (ChapterIntentCandidateModel c : intent.getCandidateCharacters()) {
                Map<String, Object> card = fetchCharacterCard(session, project.getProjectId(), c.getName());
                if (card != null) {
                    result.characterStateCards.put(c.getName(), card);
                }
            }

            result.keyRelations = fetchKeyRelations(session, project.getProjectId(), povCandidates);

            for (ChapterIntentCandidateModel cl : intent.getTargetClues()) {
                Map<String, Object> card = fetchClueProgress(session, project.getProjectId(), cl.getName());
                if (card != null) {
                    result.targetClueProgress.put(cl.getName(), card);
                }
            }

            for (ChapterIntentCandidateModel l : intent.getCandidateLocations()) {
                Map<String, Object> card = fetchLocationCard(session, project.getProjectId(), l.getName());
                if (card != null) {
                    result.locationCards.put(l.getName(), card);
                }
            }

            if (StringUtils.isNotBlank(pov)) {
                Map<String, Object> povLocation = fetchPovCurrentLocation(session, project.getProjectId(), pov);
                if (povLocation != null) {
                    result.povCurrentLocation = povLocation;
                }
            }
        }

        return result;
    }

    private String fetchPreviousChapterSummary(Session session, Long projectId, Integer chapterNo) {
        if (chapterNo == null || chapterNo <= 1) return null;
        var result = session.run(
                "MATCH (ch:Chapter {projectId: $projectId, number: $number}) RETURN ch.summary AS summary",
                Map.of("projectId", projectId, "number", chapterNo - 1));
        return result.hasNext() ? result.single().get("summary", (String) null) : null;
    }

    private List<String> fetchRecentChapterSummaries(Session session, Long projectId, Integer chapterNo, int count) {
        List<String> summaries = new ArrayList<>();
        int start = Math.max(1, (chapterNo == null ? 1 : chapterNo - count));
        int end = (chapterNo == null ? 1 : chapterNo - 1);
        for (int n = start; n <= end; n++) {
            var result = session.run(
                    "MATCH (ch:Chapter {projectId: $projectId, number: $number}) RETURN ch.summary AS summary",
                    Map.of("projectId", projectId, "number", n));
            if (result.hasNext()) {
                String s = result.single().get("summary", (String) null);
                if (StringUtils.isNotBlank(s)) summaries.add(s);
            }
        }
        return summaries;
    }

    /**
     * 查一个角色的完整状态卡——他是什么定位、现在什么情绪、目标是什么、人在哪。
     *
     * 返回的只是原始数据，压缩成一行格式（比如"李四[主角·ACTIVE·愤怒] 目标：复仇 位于旧钟楼"）
     * 是在 NovelLLMService 里做的，这里不管。
     */
    private Map<String, Object> fetchCharacterCard(Session session, Long projectId, String name) {
        var result = session.run(
                "MATCH (c:Character {projectId: $projectId, name: $name}) " +
                        "OPTIONAL MATCH (c)-[:CURRENTLY_AT {projectId: $projectId}]->(loc:Location {projectId: $projectId}) " +
                        "RETURN c.name AS name, c.role AS role, c.currentGoal AS goal, c.currentEmotion AS emotion, " +
                        "c.status AS status, c.description AS description, loc.name AS location",
                Map.of("projectId", projectId, "name", name));
        if (!result.hasNext()) return null;
        Record r = result.single();
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("name", r.get("name", (String) null));
        card.put("role", r.get("role", (String) null));
        card.put("goal", r.get("goal", (String) null));
        card.put("emotion", r.get("emotion", (String) null));
        card.put("status", r.get("status", (String) null));
        card.put("description", StringUtils.abbreviate(r.get("description", ""), 100));
        String loc = r.get("location", (String) null);
        if (StringUtils.isNotBlank(loc)) card.put("currentLocation", loc);
        return card;
    }

    private List<String> fetchKeyRelations(Session session, Long projectId, List<String> povNames) {
        Set<String> rels = new LinkedHashSet<>();
        if (povNames.isEmpty()) return new ArrayList<>(rels);
        var result = session.run(
                "MATCH (a:Character {projectId: $projectId})-[r:HATES|LOVES|IS_FAMILY_OF {projectId: $projectId}]-(b:Character {projectId: $projectId}) " +
                        "WHERE a.name IN $names " +
                        "RETURN a.name AS a, type(r) AS rel, r.intensity AS intensity, r.status AS status, r.familyType AS familyType, b.name AS b",
                Map.of("projectId", projectId, "names", povNames));
        while (result.hasNext()) {
            Record rec = result.next();
            String a = rec.get("a", "?");
            String rel = rec.get("rel", "RELATED");
            String b = rec.get("b", "?");
            StringBuilder sb = new StringBuilder();
            sb.append(a).append(" -[").append(rel);
            if (!rec.get("intensity").isNull()) sb.append(" intensity:").append(rec.get("intensity").asInt());
            if (!rec.get("status").isNull()) sb.append(" ").append(rec.get("status").asString());
            if (!rec.get("familyType").isNull()) sb.append(" ").append(rec.get("familyType").asString());
            sb.append("]-> ").append(b);
            rels.add(sb.toString());
        }
        return new ArrayList<>(rels);
    }

    private Map<String, Object> fetchClueProgress(Session session, Long projectId, String name) {
        var result = session.run(
                "MATCH (cl:Clue {projectId: $projectId, name: $name}) " +
                        "RETURN cl.name AS name, cl.type AS type, cl.status AS status, " +
                        "cl.summary AS summary, cl.revealLevel AS revealLevel, cl.priority AS priority",
                Map.of("projectId", projectId, "name", name));
        if (!result.hasNext()) return null;
        Record r = result.single();
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("name", r.get("name", (String) null));
        card.put("type", r.get("type", (String) null));
        card.put("status", r.get("status", (String) null));
        card.put("summary", r.get("summary", (String) null));
        if (!r.get("revealLevel").isNull()) card.put("revealLevel", r.get("revealLevel").asDouble());
        if (!r.get("priority").isNull()) card.put("priority", r.get("priority").asInt());
        return card;
    }

    private Map<String, Object> fetchLocationCard(Session session, Long projectId, String name) {
        var result = session.run(
                "MATCH (l:Location {projectId: $projectId, name: $name}) " +
                        "RETURN l.name AS name, l.type AS type, l.description AS description",
                Map.of("projectId", projectId, "name", name));
        if (!result.hasNext()) return null;
        Record r = result.single();
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("name", r.get("name", (String) null));
        card.put("type", r.get("type", (String) null));
        card.put("description", StringUtils.abbreviate(r.get("description", ""), 100));
        return card;
    }

    private Map<String, Object> fetchPovCurrentLocation(Session session, Long projectId, String povName) {
        var result = session.run(
                "MATCH (c:Character {projectId: $projectId, name: $name})-[:CURRENTLY_AT {projectId: $projectId}]->(l:Location {projectId: $projectId}) " +
                        "RETURN l.name AS name, l.type AS type, l.description AS description",
                Map.of("projectId", projectId, "name", povName));
        if (!result.hasNext()) return null;
        Record r = result.single();
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("name", r.get("name", (String) null));
        card.put("type", r.get("type", (String) null));
        card.put("description", StringUtils.abbreviate(r.get("description", ""), 100));
        return card;
    }

    public static class RetrievalResult {
        public Long projectId;
        public Integer chapterNo;
        public String pov;
        public String previousChapterSummary;
        public List<String> chapterSummaries = new ArrayList<>();
        public Map<String, Map<String, Object>> characterStateCards = new LinkedHashMap<>();
        public List<String> keyRelations = new ArrayList<>();
        public Map<String, Map<String, Object>> targetClueProgress = new LinkedHashMap<>();
        public Map<String, Map<String, Object>> locationCards = new LinkedHashMap<>();
        public Map<String, Object> povCurrentLocation;
        public int estimatedTokens;
        public int truncatedItems;
    }
}
