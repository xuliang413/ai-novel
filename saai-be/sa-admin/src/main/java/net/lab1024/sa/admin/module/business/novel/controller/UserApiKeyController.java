package net.lab1024.sa.admin.module.business.novel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.module.business.novel.dao.WritingLogDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.WritingLogEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.UserApiKeySaveForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.UserApiKeyTestForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.UserApiKeyTestVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.UserApiKeyVO;
import net.lab1024.sa.admin.module.business.novel.service.UserApiKeyService;
import net.lab1024.sa.admin.util.AdminRequestUtil;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AI 小说用户 API Key 接口。
 */
@RestController
@RequestMapping("/novel/user-api-key")
@Tag(name = "AI 小说 - 用户 API Key")
public class UserApiKeyController {

    @Resource
    private UserApiKeyService userApiKeyService;

    @Resource
    private WritingLogDao writingLogDao;

    @Operation(summary = "保存当前用户 API Key")
    @PostMapping("/save")
    public ResponseDTO<Boolean> save(@RequestBody @Valid UserApiKeySaveForm form) {
        return userApiKeyService.save(form);
    }

    @Operation(summary = "获取当前用户 API Key 配置")
    @PostMapping("/get")
    public ResponseDTO<UserApiKeyVO> get() {
        return userApiKeyService.get();
    }

    @Operation(summary = "检测当前用户 API Key 配置")
    @PostMapping("/test")
    public ResponseDTO<UserApiKeyTestVO> test(@RequestBody @Valid UserApiKeyTestForm form) {
        return userApiKeyService.test(form);
    }

    /**
     * API Key 用量统计 —— 按模型厂商（DeepSeek/Qwen）统计调用次数和 Token 消耗
     *
     * 数据来源：writing_log 表，每次成功或失败的 LLM 调用都会写入一条。
     * 统计维度：provider（DEEPSEEK/TONGYI/MOCK）、本月/本周/今日。
     *
     * 用户可以在这里看到自己的 Key 消耗了多少，是否需要充值。
     */
    @Operation(summary = "获取当前用户 API Key 用量")
    @PostMapping("/usage")
    public ResponseDTO<Map<String, Object>> usage() {
        Long userId = AdminRequestUtil.getRequestUserId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime dayStart = now.withHour(0).withMinute(0).withSecond(0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("month", buildStats(userId, monthStart, now));
        result.put("week", buildStats(userId, weekStart, now));
        result.put("today", buildStats(userId, dayStart, now));
        return ResponseDTO.ok(result);
    }

    /**
     * 指定时间范围内的按 provider 统计
     */
    private Map<String, Object> buildStats(Long userId, LocalDateTime from, LocalDateTime to) {
        List<WritingLogEntity> logs = writingLogDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WritingLogEntity>()
                        .eq(WritingLogEntity::getCreateUserId, userId)
                        .between(WritingLogEntity::getCreateTime, from, to));

        Map<String, Object> stats = new LinkedHashMap<>();
        int totalCalls = logs.size();
        int successCalls = 0;
        int failedCalls = 0;
        int totalTokens = 0;

        Map<String, Integer> byProvider = new LinkedHashMap<>();
        for (WritingLogEntity log : logs) {
            String provider = log.getProvider() != null ? log.getProvider() : "UNKNOWN";
            byProvider.merge(provider, 1, Integer::sum);
            if (Boolean.TRUE.equals(log.getSuccess())) {
                successCalls++;
            } else {
                failedCalls++;
            }
            if (log.getTokenUsed() != null) {
                totalTokens += log.getTokenUsed();
            }
        }

        stats.put("totalCalls", totalCalls);
        stats.put("successCalls", successCalls);
        stats.put("failedCalls", failedCalls);
        stats.put("totalTokens", totalTokens);
        stats.put("byProvider", byProvider);
        return stats;
    }
}
