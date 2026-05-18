package net.lab1024.sa.admin.module.business.novel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.novel.domain.form.UserApiKeyAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.UserApiKeyVO;
import net.lab1024.sa.admin.module.business.novel.service.UserApiKeyService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户API Key管理接口。
 * <p>
 * 所有操作以 Sa-Token 上下文中的当前用户ID为准，不接受外部传入userId，
 * 防止通过URL猜测userId横向越权。
 *
 * @Author AI-Novel
 */
@RestController
@Tag(name = AdminSwaggerTagConst.Business.NOVEL_USER_API_KEY)
@RequestMapping("/novel/userApiKey")
public class UserApiKeyController {

    @Resource
    private UserApiKeyService userApiKeyService;

    /**
     * 保存/更新当前用户的API Key。
     * <p>
     * 同一种modelType只保留一条记录(uk_user_model_type保证)，再次提交即为更新。
     *
     * @param addForm 表单(modelType/apiKey/modelName等)
     * @return 操作结果
     */
    @Operation(summary = "保存/更新API Key @author AI-Novel")
    @PostMapping("/save")
    public ResponseDTO<String> save(@RequestBody @Valid UserApiKeyAddForm addForm) {
        return userApiKeyService.saveApiKey(addForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 获取当前用户所有API Key配置(脱敏)。
     * <p>
     * Key脱敏格式: sk-****abcd，前端可提供"显示"按钮调test接口验证。
     *
     * @return Key列表，已脱敏
     */
    @Operation(summary = "获取当前用户API Key列表(脱敏) @author AI-Novel")
    @GetMapping("/list")
    public ResponseDTO<List<UserApiKeyVO>> list() {
        return userApiKeyService.listUserKeys(SmartRequestUtil.getRequestUserId());
    }

    /**
     * 测试指定Key的连接性 —— 后端发短文本生成请求验证Key可用。
     *
     * @param id Key记录ID
     * @return 测试结果
     */
    @Operation(summary = "测试API Key连接 @author AI-Novel")
    @PostMapping("/test")
    public ResponseDTO<String> testConnection(@RequestParam Long id) {
        return userApiKeyService.testConnection(id, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 删除当前用户某条API Key(软删除)。
     * <p>
     * 标记 deletedFlag=1，可取消删除恢复。
     *
     * @param id Key记录ID
     * @return 删除结果
     */
    @Operation(summary = "删除API Key @author AI-Novel")
    @PostMapping("/delete")
    public ResponseDTO<String> delete(@RequestParam Long id) {
        return userApiKeyService.deleteApiKey(id, SmartRequestUtil.getRequestUserId());
    }
}
