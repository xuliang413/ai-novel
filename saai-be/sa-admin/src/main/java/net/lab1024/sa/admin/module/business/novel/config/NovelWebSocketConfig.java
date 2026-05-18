package net.lab1024.sa.admin.module.business.novel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import jakarta.annotation.Resource;

/**
 * 小说WebSocket配置 —— 注册写作流式推送端点
 * <p>
 * 端点路径: /ws/novel/write, 前端通过 ws://host:port/ws/novel/write?token=xxx 连接。
 * 握手时校验Sa-Token, 断线后后端继续生成完成, P1阶段支持/recover拉回。
 *
 * @Author AI-Novel
 */
@Configuration
@EnableWebSocket
public class NovelWebSocketConfig implements WebSocketConfigurer {

    /**
     * 写作流式推送处理器, 负责管理每个连接的写作会话和逐token推送
     */
    @Resource
    private NovelWriteWebSocketHandler novelWriteWebSocketHandler;

    /**
     * WebSocket握手拦截器, 在连接建立前校验Sa-Token和写作权限
     */
    @Resource
    private NovelWriteHandshakeInterceptor novelWriteHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(novelWriteWebSocketHandler, "/ws/novel/write")
                .addInterceptors(novelWriteHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
