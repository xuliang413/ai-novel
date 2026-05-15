package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.novel.dao.UserApiKeyDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.UserApiKeyEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.UserApiKeySaveForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.UserApiKeyTestForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.UserApiKeyTestVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.UserApiKeyVO;
import net.lab1024.sa.admin.util.AdminRequestUtil;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.module.support.apiencrypt.service.ApiEncryptService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户大模型 API Key 服务。
 *
 * 这里管的是“当前登录用户自己的 Key”，不是系统级 Key。
 * 写作时 NovelLLMConfig 会再从同一张表取出并解密。
 */
@Service
public class UserApiKeyService {

    private static final String PROVIDER_DEEPSEEK = "DEEPSEEK";

    private static final String PROVIDER_TONGYI = "TONGYI";

    @Resource
    private UserApiKeyDao userApiKeyDao;

    @Resource
    private ApiEncryptService apiEncryptService;

    /**
     * 保存当前用户的 API Key，使用 SmartAdmin 的加密服务落库。
     *
     * 表单字段为 null 表示“不改这个 Key”；空字符串表示“清空这个 Key”。
     * 这样前端只改 DeepSeek 时，不会误删通义千问。
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> save(UserApiKeySaveForm form) {
        Long userId = AdminRequestUtil.getRequestUserId();
        UserApiKeyEntity entity = getByUserId(userId);
        if (entity == null) {
            entity = new UserApiKeyEntity();
            entity.setUserId(userId);
        }

        if (form.getDeepseekKey() != null) {
            // 前端传了这个字段才更新；encryptOrBlank 会处理清空和加密两种情况。
            entity.setDeepseekKey(encryptOrBlank(form.getDeepseekKey()));
        }
        if (form.getQwenKey() != null) {
            entity.setQwenKey(encryptOrBlank(form.getQwenKey()));
        }

        if (entity.getId() == null) {
            userApiKeyDao.insert(entity);
        } else {
            userApiKeyDao.updateById(entity);
        }
        return ResponseDTO.ok(true);
    }

    /**
     * 获取当前用户 API Key 配置，默认只返回脱敏信息。
     *
     * 不直接返回明文 Key，避免前端日志、浏览器插件或截图把 Key 泄露出去。
     */
    public ResponseDTO<UserApiKeyVO> get() {
        UserApiKeyEntity entity = getByUserId(AdminRequestUtil.getRequestUserId());
        UserApiKeyVO vo = new UserApiKeyVO();
        if (entity == null) {
            vo.setHasDeepseekKey(false);
            vo.setHasQwenKey(false);
            vo.setDeepseekStatus("MISSING");
            vo.setQwenStatus("MISSING");
            return ResponseDTO.ok(vo);
        }

        String deepseek = decryptOrPlain(entity.getDeepseekKey());
        String qwen = decryptOrPlain(entity.getQwenKey());
        vo.setHasDeepseekKey(StringUtils.isNotBlank(deepseek));
        vo.setHasQwenKey(StringUtils.isNotBlank(qwen));
        vo.setDeepseekMasked(mask(deepseek));
        vo.setQwenMasked(mask(qwen));
        vo.setDeepseekStatus(StringUtils.isNotBlank(deepseek) ? "CONFIGURED" : "MISSING");
        vo.setQwenStatus(StringUtils.isNotBlank(qwen) ? "CONFIGURED" : "MISSING");
        vo.setUpdateTime(entity.getUpdateTime());
        return ResponseDTO.ok(vo);
    }

    /**
     * 本地连接状态检测：校验是否已配置可解密/可识别的 Key，不主动发外部请求。
     *
     * 这里先不调用 DeepSeek / 通义千问接口，原因是设置页可能被频繁打开；
     * 真正的外部调用放在写作生成时处理，失败后会走 mock 降级。
     */
    public ResponseDTO<UserApiKeyTestVO> test(UserApiKeyTestForm form) {
        String provider = StringUtils.upperCase(form.getProvider());
        UserApiKeyEntity entity = getByUserId(AdminRequestUtil.getRequestUserId());
        String key = null;
        if (entity != null && PROVIDER_DEEPSEEK.equals(provider)) {
            key = decryptOrPlain(entity.getDeepseekKey());
        } else if (entity != null && PROVIDER_TONGYI.equals(provider)) {
            key = decryptOrPlain(entity.getQwenKey());
        }

        UserApiKeyTestVO vo = new UserApiKeyTestVO();
        vo.setProvider(provider);
        vo.setConfigured(StringUtils.isNotBlank(key));
        vo.setStatus(StringUtils.isNotBlank(key) ? "CONFIGURED" : "MISSING");
        vo.setMessage(StringUtils.isNotBlank(key) ? "API Key 已配置，可用于下次生成。" : "当前用户未配置该供应商 API Key。");
        return ResponseDTO.ok(vo);
    }

    /**
     * 当前表按 user_id 唯一，但这里仍然 limit 1，避免历史脏数据导致查询异常。
     */
    private UserApiKeyEntity getByUserId(Long userId) {
        return userApiKeyDao.selectOne(new LambdaQueryWrapper<UserApiKeyEntity>()
                .eq(UserApiKeyEntity::getUserId, userId)
                .last("limit 1"));
    }

    /**
     * 加密前先 trim；空字符串直接存空，表示用户主动清空。
     */
    private String encryptOrBlank(String key) {
        if (StringUtils.isBlank(key)) {
            return "";
        }
        return apiEncryptService.encrypt(key.trim());
    }

    /**
     * 取 Key 时兼容两种历史情况：
     * 1. 新流程：数据库里是加密串，解密后使用；
     * 2. 老数据/手动写入：看起来像明文 Key，就直接使用。
     */
    private String decryptOrPlain(String encrypted) {
        if (StringUtils.isBlank(encrypted)) {
            return "";
        }
        String decrypted = apiEncryptService.decrypt(encrypted);
        if (StringUtils.isNotBlank(decrypted)) {
            return decrypted;
        }
        if (encrypted.startsWith("sk-") || encrypted.contains("dashscope")) {
            return encrypted;
        }
        return "";
    }

    /**
     * 前端展示用的脱敏文本，只让用户确认“大概是哪一个 Key”。
     */
    private String mask(String key) {
        if (StringUtils.isBlank(key)) {
            return "";
        }
        if (key.length() <= 8) {
            return "****";
        }
        return key.substring(0, 3) + "****" + key.substring(key.length() - 4);
    }
}
