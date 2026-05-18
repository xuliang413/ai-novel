package net.lab1024.sa.admin.module.business.novel.config;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手拦截器 —— 在连接建立前从URL参数中校验Sa-Token
 * <p>
 * 前端连接: ws://host:port/ws/novel/write?token=xxx
 * 校验通过后将用户ID注入WebSocket Session attributes, 后续消息处理可直接使用。
 *
 * @Author AI-Novel
 */
@Slf4j
@Component
public class NovelWriteHandshakeInterceptor implements HandshakeInterceptor {

    /**
     * 握手前校验: 从URL参数提取token, 通过Sa-Token验证后注入用户ID
     *
     * @param request 握手请求
     * @param response 握手响应
     * @param wsHandler WebSocket处理器
     * @param attributes WebSocket会话属性, 验证通过后注入userId
     * @return true=握手继续, false=拒绝连接
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();
        if (query == null || !query.contains("token=")) {
            log.warn("WebSocket握手失败: 缺少token参数");
            return false;
        }

        String token = extractToken(query);
        if (token == null || token.isEmpty()) {
            log.warn("WebSocket握手失败: token为空");
            return false;
        }

        try {
            // 通过Sa-Token校验
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                log.warn("WebSocket握手失败: token无效");
                return false;
            }
            // 将用户ID注入WebSocket session attributes
            attributes.put("userId", Long.parseLong(loginId.toString()));
            attributes.put("token", token);
            log.info("WebSocket握手成功, userId={}", loginId);
            return true;
        } catch (Exception e) {
            log.error("WebSocket握手异常", e);
            return false;
        }
    }

    /**
     * 握手后处理, 无需额外操作
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }

    /**
     * 从URL查询参数中提取token值
     */
    private String extractToken(String query) {
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                return param.substring(6);
            }
        }
        return null;
    }
}
