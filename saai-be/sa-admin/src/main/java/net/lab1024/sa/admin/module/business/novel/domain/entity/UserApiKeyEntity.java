package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户 AI Key 表。存储 DeepSeek 和通义千问的 Key，加密保存。
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
