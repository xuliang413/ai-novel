package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.lab1024.sa.admin.module.business.novel.config.NovelLLMConfig;
import net.lab1024.sa.admin.module.business.novel.dao.UserApiKeyDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.UserApiKeyEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.UserApiKeyAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.UserApiKeyVO;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.module.support.apiencrypt.service.ApiEncryptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserApiKeyService 单元测试。
 * <p>
 * 覆盖 增删查 + 用户隔离 + 脱敏 + 连接测试归属校验 四条关键行为。
 *
 * @Author AI-Novel
 */
class UserApiKeyServiceTest {

    private UserApiKeyService userApiKeyService;
    private UserApiKeyDao userApiKeyDao;
    private ApiEncryptService apiEncryptService;
    private NovelLLMConfig novelLLMConfig;

    private final Long userId = 1001L;

    @BeforeEach
    void setup() {
        userApiKeyService = new UserApiKeyService();
        userApiKeyDao = mock(UserApiKeyDao.class);
        apiEncryptService = mock(ApiEncryptService.class);
        novelLLMConfig = mock(NovelLLMConfig.class);

        ReflectionTestUtils.setField(userApiKeyService, "userApiKeyDao", userApiKeyDao);
        ReflectionTestUtils.setField(userApiKeyService, "apiEncryptService", apiEncryptService);
        ReflectionTestUtils.setField(userApiKeyService, "novelLLMConfig", novelLLMConfig);
    }

    // ======================== 保存测试 ========================

    @Test
    void saveShouldEncryptKeyAndPersist() {
        UserApiKeyAddForm form = buildForm();
        when(userApiKeyDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(apiEncryptService.encrypt(anyString())).thenReturn("ENCRYPTED_KEY");
        when(userApiKeyDao.insert(any(UserApiKeyEntity.class))).thenReturn(1);
        ResponseDTO<String> result = userApiKeyService.saveApiKey(form, userId);

        assertTrue(result.getOk());
        verify(apiEncryptService, times(1)).encrypt("sk-test-key");
        verify(userApiKeyDao, times(1)).insert(any(UserApiKeyEntity.class));
    }

    @Test
    void saveShouldUpdateExistingKey() {
        UserApiKeyAddForm form = buildForm();
        UserApiKeyEntity existing = buildEntity(1L);
        when(userApiKeyDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(apiEncryptService.encrypt(anyString())).thenReturn("ENCRYPTED_KEY");

        ResponseDTO<String> result = userApiKeyService.saveApiKey(form, userId);

        assertTrue(result.getOk());
        verify(userApiKeyDao, times(1)).updateById(any(UserApiKeyEntity.class));
    }

    // ======================== 查询&脱敏测试 ========================

    @Test
    void listShouldMaskApiKey() {
        UserApiKeyEntity entity = buildEntity(1L);
        entity.setApiKey("ENCRYPTED_abcdefgh12345678");
        when(userApiKeyDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(entity));
        when(apiEncryptService.decrypt(anyString())).thenReturn("sk-1234567890abcdef");

        ResponseDTO<List<UserApiKeyVO>> result = userApiKeyService.listUserKeys(userId);

        assertTrue(result.getOk());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().get(0).getApiKey().contains("****"), "Key应脱敏");
        assertFalse(result.getData().get(0).getApiKey().contains("1234567890"), "不应包含完整Key");
    }

    // ======================== 删除&隔离测试 ========================

    @Test
    void deleteShouldRejectNonOwner() {
        UserApiKeyEntity entity = buildEntity(1L);
        entity.setUserId(9999L); // 其他人的Key
        when(userApiKeyDao.selectById(1L)).thenReturn(entity);

        ResponseDTO<String> result = userApiKeyService.deleteApiKey(1L, userId);

        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("无权删除"));
    }

    @Test
    void deleteShouldSoftDeleteOwnKey() {
        UserApiKeyEntity entity = buildEntity(1L);
        when(userApiKeyDao.selectById(1L)).thenReturn(entity);

        ResponseDTO<String> result = userApiKeyService.deleteApiKey(1L, userId);

        assertTrue(result.getOk());
        verify(userApiKeyDao, times(1)).updateById(any(UserApiKeyEntity.class));
    }

    // ======================== 连接测试&归属校验 ========================

    @Test
    void testConnectionShouldRejectNonOwner() {
        UserApiKeyEntity entity = buildEntity(1L);
        entity.setUserId(9999L);
        when(userApiKeyDao.selectById(1L)).thenReturn(entity);

        ResponseDTO<String> result = userApiKeyService.testConnection(1L, userId);

        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("无权测试"));
    }

    @Test
    void testConnectionShouldRejectEmptyKey() {
        UserApiKeyEntity entity = buildEntity(1L);
        entity.setApiKey(null);
        when(userApiKeyDao.selectById(1L)).thenReturn(entity);

        ResponseDTO<String> result = userApiKeyService.testConnection(1L, userId);

        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("为空"));
    }

    // ======================== Helpers ========================

    private UserApiKeyAddForm buildForm() {
        UserApiKeyAddForm form = new UserApiKeyAddForm();
        form.setModelType("CHAT");
        form.setApiKey("sk-test-key");
        form.setUrl("https://api.deepseek.com");
        form.setModelName("deepseek-chat");
        form.setTemperature(BigDecimal.valueOf(0.7));
        form.setMaxTokens(4096);
        form.setTimeout(60000);
        return form;
    }

    private UserApiKeyEntity buildEntity(Long id) {
        UserApiKeyEntity entity = new UserApiKeyEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setModelType("CHAT");
        entity.setApiKey("ENCRYPTED_VALUE");
        entity.setDeletedFlag(false);
        return entity;
    }
}
