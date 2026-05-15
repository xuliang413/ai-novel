package net.lab1024.sa.admin.module.business.novel.config;

import net.lab1024.sa.admin.module.business.novel.ws.NovelWriteWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置。
 *
 * 注册写作流式推送端点 /ws/novel/write，
 * 鉴权在 Handler 的 afterConnectionEstablished 中通过首条消息的 token 校验。
 */
@Configuration
@EnableWebSocket
public class NovelWebSocketConfig implements WebSocketConfigurer {

    private final NovelWriteWebSocketHandler writeHandler;

    public NovelWebSocketConfig(NovelWriteWebSocketHandler writeHandler) {
        this.writeHandler = writeHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(writeHandler, "/ws/novel/write")
                .setAllowedOrigins("*");
    }
}
