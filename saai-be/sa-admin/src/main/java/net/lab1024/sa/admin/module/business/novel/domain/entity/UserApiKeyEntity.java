package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户 AI Key 表。存储 DeepSeek 和通义千问的 Key，加密保存。
 *
 * 注意它是用户级，不是项目级；同一个用户写不同小说共用一套模型 Key。
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

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}
