package net.lab1024.sa.admin.module.business.novel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.module.business.novel.domain.form.UserApiKeySaveForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.UserApiKeyTestForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.UserApiKeyTestVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.UserApiKeyVO;
import net.lab1024.sa.admin.module.business.novel.service.UserApiKeyService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 小说用户 API Key 接口。
 */
@RestController
@RequestMapping("/novel/user-api-key")
@Tag(name = "AI 小说 - 用户 API Key")
public class UserApiKeyController {

    @Resource
    private UserApiKeyService userApiKeyService;

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
}
