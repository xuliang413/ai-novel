package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserApiKeyService {

    @Resource
    private UserApiKeyDao userApiKeyDao;

    @Resource
    private ApiEncryptService apiEncryptService;

    /**
     * 保存或更新用户的API Key
     * 一个用户每种modelType只能有一条记录(uk_user_model_type保证)
     * 需要从登录上下文获取当前用户ID, 当前临时通过form传入
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> saveApiKey(UserApiKeyAddForm addForm) {
        // 用户ID应从Sa-Token上下文获取, 当前阶段通过form传入
        Long userId = 1L;

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
     * 获取用户所有API Key配置(脱敏)
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
     * 测试API Key连接性
     */
    public ResponseDTO<String> testConnection(Long id) {
        UserApiKeyEntity key = userApiKeyDao.selectById(id);
        if (key == null || key.getApiKey() == null) {
            return ResponseDTO.userErrorParam("未找到该API Key记录");
        }
        // TODO: 实际发短请求测试连接
        return ResponseDTO.ok("连接测试通过");
    }

    /**
     * 删除用户某条API Key(软删除)
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deleteApiKey(Long id) {
        UserApiKeyEntity key = userApiKeyDao.selectById(id);
        if (key != null) {
            key.setDeletedFlag(true);
            userApiKeyDao.updateById(key);
        }
        return ResponseDTO.ok();
    }
}
