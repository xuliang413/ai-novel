package net.lab1024.sa.admin.module.business.novel.domain.vo;

import lombok.Data;

/**
 * 用户 API Key 检测结果。
 *
 * 当前检测偏本地：判断是否配置、是否能解密或看起来像明文 Key。
 */
@Data
public class UserApiKeyTestVO {

    /**
     * 检测的供应商。
     */
    private String provider;

    /**
     * 是否已经配置。
     */
    private Boolean configured;

    /**
     * 检测状态，例如 OK / MISSING。
     */
    private String status;

    /**
     * 展示给用户看的说明。
     */
    private String message;
}
