package com.shingeki.travelplannerbackend.controller;

import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * 科大讯飞 WebSocket 客户端
 */
@Slf4j
@ClientEndpoint
public class XFYunWebSocketClient {

    private final WebSocketSession springSession;

    public XFYunWebSocketClient(WebSocketSession springSession) {
        this.springSession = springSession;
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("✅ 科大讯飞 WebSocket 连接建立: {}", session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.debug("收到科大讯飞识别结果: {}", message);
        
        // 转发识别结果给前端
        try {
            if (springSession.isOpen()) {
                springSession.sendMessage(new TextMessage(message));
            }
        } catch (Exception e) {
            log.error("转发识别结果失败", e);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.info("科大讯飞 WebSocket 连接关闭: {}", closeReason);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("科大讯飞 WebSocket 错误", error);
    }
}
