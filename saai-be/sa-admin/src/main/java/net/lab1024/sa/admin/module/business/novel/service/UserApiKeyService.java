package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.config.NovelLLMConfig;
import net.lab1024.sa.admin.module.business.novel.dao.UserApiKeyDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.UserApiKeyEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.UserApiKeyAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.UserApiKeyVO;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.module.support.apiencrypt.service.ApiEncryptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户API Key管理服务。
 * <p>
 * 每个用户每种modelType只能有一条记录(uk_user_model_type保证)。
 * 所有操作以 token context 中的 userId 为主键，不接受外部传入userId。
 *
 * @Author AI-Novel
 */
@Slf4j
@Service
public class UserApiKeyService {

    @Resource
    private UserApiKeyDao userApiKeyDao;

    @Resource
    private ApiEncryptService apiEncryptService;

    @Resource
    private NovelLLMConfig novelLLMConfig;

    /**
     * 保存或更新当前用户的API Key。
     * <p>
     * userId从 token context 获取，不接受外部传入，防止修改他人Key。
     * 存在则更新（保留原有ID），不存在则新增。
     *
     * @param addForm 表单
     * @param userId 当前登录用户ID
     * @return 操作结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> saveApiKey(UserApiKeyAddForm addForm, Long userId) {
        UserApiKeyEntity existing = userApiKeyDao.selectOne(new LambdaQueryWrapper<UserApiKeyEntity>()
                .eq(UserApiKeyEntity::getUserId, userId)
                .eq(UserApiKeyEntity::getModelType, addForm.getModelType()));

        UserApiKeyEntity entity = existing != null ? existing : new UserApiKeyEntity();
        entity.setUserId(userId);
        entity.setModelType(addForm.getModelType());
        entity.setUrl(addForm.getUrl());
        if (addForm.getApiKey() != null && !addForm.getApiKey().isEmpty()) {
            entity.setApiKey(apiEncryptService.encrypt(addForm.getApiKey()));
        }
        entity.setModelName(addForm.getModelName());
        entity.setProviderName(addForm.getProviderName());
        entity.setTemperature(addForm.getTemperature());
        entity.setMaxTokens(addForm.getMaxTokens());
        entity.setTimeout(addForm.getTimeout());
        entity.setDeletedFlag(false);

        if (existing != null) {
            userApiKeyDao.updateById(entity);
        } else {
            userApiKeyDao.insert(entity);
        }
        return ResponseDTO.ok();
    }

    /**
     * 获取当前用户所有API Key配置(脱敏)。
     *
     * @param userId 当前登录用户ID
     * @return Key列表, 已脱敏
     */
    public ResponseDTO<List<UserApiKeyVO>> listUserKeys(Long userId) {
        List<UserApiKeyEntity> keys = userApiKeyDao.selectList(new LambdaQueryWrapper<UserApiKeyEntity>()
                .eq(UserApiKeyEntity::getUserId, userId)
                .eq(UserApiKeyEntity::getDeletedFlag, false));

        List<UserApiKeyVO> vos = keys.stream().map(entity -> {
            UserApiKeyVO vo = SmartBeanUtil.copy(entity, UserApiKeyVO.class);
            if (entity.getApiKey() != null && entity.getApiKey().length() > 8) {
                String decrypted = apiEncryptService.decrypt(entity.getApiKey());
                vo.setApiKey(decrypted.substring(0, 3) + "****" + decrypted.substring(decrypted.length() - 4));
            }
            return vo;
        }).collect(Collectors.toList());

        return ResponseDTO.ok(vos);
    }

    /**
     * 测试API Key连接性 —— 针对当前用户的一条已保存Key发短请求。
     * <p>
     * 校验该Key归属当前用户后，用NovelLLMConfig临时创建模型对象发一条短text生成请求。
     *
     * @param id Key记录ID
     * @param userId 当前登录用户ID
     * @return 测试结果
     */
    public ResponseDTO<String> testConnection(Long id, Long userId) {
        UserApiKeyEntity key = userApiKeyDao.selectById(id);
        if (key == null) {
            return ResponseDTO.userErrorParam("未找到该API Key记录");
        }
        if (!Objects.equals(key.getUserId(), userId)) {
            return ResponseDTO.userErrorParam("无权测试他人的API Key");
        }
        if (key.getApiKey() == null || key.getApiKey().isEmpty()) {
            return ResponseDTO.userErrorParam("API Key为空, 请先填写");
        }

        try {
            // 临时用Chat模型发一条短text生成请求, 验证Key+地址+模型名组合可用
            var model = novelLLMConfig.createChatModel(userId);
            if (model == null) {
                return ResponseDTO.userErrorParam("创建模型对象失败, 请检查Key配置");
            }
            model.generate("测试连接，请回复ok"); // 最短请求，有回复就说明通
            return ResponseDTO.ok("连接测试通过");
        } catch (Exception e) {
            log.error("Key连接测试失败 id={}, userId={}", id, userId, e);
            return ResponseDTO.userErrorParam("连接测试失败: " + e.getMessage());
        }
    }

    /**
     * 删除当前用户某条API Key(软删除)。
     * <p>
     * 先校验该Key归属当前用户，防止删除他人Key。
     *
     * @param id Key记录ID
     * @param userId 当前登录用户ID
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deleteApiKey(Long id, Long userId) {
        UserApiKeyEntity key = userApiKeyDao.selectById(id);
        if (key == null) {
            return ResponseDTO.userErrorParam("未找到该API Key记录");
        }
        if (!Objects.equals(key.getUserId(), userId)) {
            return ResponseDTO.userErrorParam("无权删除他人的API Key");
        }
        key.setDeletedFlag(true);
        userApiKeyDao.updateById(key);
        return ResponseDTO.ok("已删除");
    }
}
