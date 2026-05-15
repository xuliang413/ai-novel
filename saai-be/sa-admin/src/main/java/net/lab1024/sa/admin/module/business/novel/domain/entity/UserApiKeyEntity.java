package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户大模型 API Key 实体。
 *
 * M0 先建表占位；真实保存时必须加密后再落库。
 */
@Data
@TableName("t_user_api_key")
public class UserApiKeyEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 加密后的 DeepSeek API Key。
     */
    private String deepseekKey;

    /**
     * 加密后的通义千问 API Key。
     */
    private String qwenKey;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
