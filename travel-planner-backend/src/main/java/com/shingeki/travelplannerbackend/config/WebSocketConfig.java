package com.shingeki.travelplannerbackend.config;

import com.shingeki.travelplannerbackend.controller.SpeechWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置类
 * 用于配置 Spring WebSocket
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SpeechWebSocketHandler speechWebSocketHandler;

    public WebSocketConfig(SpeechWebSocketHandler speechWebSocketHandler) {
        this.speechWebSocketHandler = speechWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(speechWebSocketHandler, "/api/speech/websocket")
                .setAllowedOrigins("*"); // 生产环境应该设置具体的域名
    }
}
