package net.lab1024.sa.admin.module.business.novel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.novel.domain.form.UserApiKeyAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.UserApiKeyUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.UserApiKeyVO;
import net.lab1024.sa.admin.module.business.novel.service.UserApiKeyService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户API Key管理接口
 *
 * @Author AI-Novel
 */
@RestController
@Tag(name = AdminSwaggerTagConst.Business.NOVEL_USER_API_KEY)
public class UserApiKeyController {

    @Resource
    private UserApiKeyService userApiKeyService;

    @Operation(summary = "保存/更新API Key")
    @PostMapping("/novel/userApiKey/save")
    public ResponseDTO<String> save(@RequestBody @Valid UserApiKeyAddForm addForm) {
        return userApiKeyService.saveApiKey(addForm);
    }

    @Operation(summary = "获取用户API Key列表(脱敏)")
    @GetMapping("/novel/userApiKey/list/{userId}")
    public ResponseDTO<List<UserApiKeyVO>> list(@PathVariable Long userId) {
        return userApiKeyService.listUserKeys(userId);
    }

    @Operation(summary = "测试API Key连接")
    @PostMapping("/novel/userApiKey/test")
    public ResponseDTO<String> testConnection(@RequestBody UserApiKeyUpdateForm updateForm) {
        return userApiKeyService.testConnection(updateForm.getId());
    }

    @Operation(summary = "删除API Key")
    @GetMapping("/novel/userApiKey/delete/{id}")
    public ResponseDTO<String> delete(@PathVariable Long id) {
        return userApiKeyService.deleteApiKey(id);
    }
}
