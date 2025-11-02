package com.shingeki.travelplannerbackend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import jakarta.websocket.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 科大讯飞语音识别 WebSocket 代理（使用 Spring WebSocket）
 * 前端连接到此 WebSocket, 后端转发到科大讯飞, 隐藏 API Key
 */
@Slf4j
@Component
public class SpeechWebSocketHandler extends BinaryWebSocketHandler {

    @Value("${xfyun.app.id:}")
    private String xfyunAppId;

    @Value("${xfyun.api.key:}")
    private String xfyunApiKey;

    @Value("${xfyun.api.secret:}")
    private String xfyunApiSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 存储前端 WebSocket 会话和对应的科大讯飞会话
    private final Map<String, Session> xfyunSessions = new ConcurrentHashMap<>();

    /**
     * 前端连接建立
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("前端 WebSocket 连接建立: {}", session.getId());

        // 检查配置
        if (xfyunAppId == null || xfyunAppId.isEmpty() || 
            xfyunApiKey == null || xfyunApiKey.isEmpty() || 
            xfyunApiSecret == null || xfyunApiSecret.isEmpty()) {
            log.error("科大讯飞配置未设置，请检查环境变量: XFYUN_APP_ID, XFYUN_API_KEY, XFYUN_API_SECRET");
            sendErrorToClient(session, "语音识别服务未配置，请联系管理员");
            session.close();
            return;
        }

        try {
            // 连接到科大讯飞
            connectToXFYun(session);
        } catch (Exception e) {
            log.error("连接科大讯飞失败", e);
            sendErrorToClient(session, "连接语音识别服务失败: " + e.getMessage());
            session.close();
        }
    }

    /**
     * 接收前端消息(文本消息 - 控制命令)
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.debug("收到前端文本消息: {}", payload);

        try {
            JsonNode node = objectMapper.readTree(payload);
            String type = node.has("type") ? node.get("type").asText() : "data";

            Session xfyunSession = xfyunSessions.get(session.getId());
            
            if ("start".equals(type)) {
                // 发送开始参数给科大讯飞
                sendStartParamsToXFYun(xfyunSession);
            } else if ("end".equals(type)) {
                // 发送结束标记给科大讯飞
                sendEndToXFYun(xfyunSession);
            } else if (xfyunSession != null && xfyunSession.isOpen()) {
                // 转发其他消息
                xfyunSession.getBasicRemote().sendText(payload);
            }
        } catch (Exception e) {
            log.error("处理前端文本消息失败", e);
        }
    }

    /**
     * 接收前端音频数据(二进制消息)
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        log.debug("收到前端音频数据: {} bytes", message.getPayloadLength());

        try {
            Session xfyunSession = xfyunSessions.get(session.getId());
            
            if (xfyunSession != null && xfyunSession.isOpen()) {
                // 构造科大讯飞需要的数据格式
                ByteBuffer buffer = message.getPayload();
                byte[] audioData = new byte[buffer.remaining()];
                buffer.get(audioData);
                
                String base64Audio = Base64.getEncoder().encodeToString(audioData);
                
                Map<String, Object> dataFrame = new HashMap<>();
                Map<String, Object> data = new HashMap<>();
                data.put("status", 1); // 1表示数据传输中
                data.put("format", "audio/L16;rate=16000");
                data.put("encoding", "raw");
                data.put("audio", base64Audio);
                dataFrame.put("data", data);

                xfyunSession.getBasicRemote().sendText(objectMapper.writeValueAsString(dataFrame));
            }
        } catch (Exception e) {
            log.error("转发音频数据失败", e);
        }
    }

    /**
     * 前端连接关闭
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("前端 WebSocket 连接关闭: {}, 状态: {}", session.getId(), status);
        closeXFYunConnection(session.getId());
    }

    /**
     * 前端连接错误
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("前端 WebSocket 错误: {}", session.getId(), exception);
        closeXFYunConnection(session.getId());
    }

    /**
     * 连接到科大讯飞
     */
    private void connectToXFYun(WebSocketSession clientSession) throws Exception {
        String authUrl = getAuthUrl();
        log.info("连接科大讯飞: {}", authUrl);

        // 使用 @ClientEndpoint 注解的客户端类
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        XFYunWebSocketClient client = new XFYunWebSocketClient(clientSession);
        Session xfyunSession = container.connectToServer(client, URI.create(authUrl));

        xfyunSessions.put(clientSession.getId(), xfyunSession);
    }

    /**
     * 发送开始参数给科大讯飞
     */
    private void sendStartParamsToXFYun(Session xfyunSession) throws Exception {
        if (xfyunSession == null || !xfyunSession.isOpen()) {
            log.warn("科大讯飞连接未建立，无法发送开始参数");
            return;
        }

        Map<String, Object> params = new HashMap<>();
        
        Map<String, Object> common = new HashMap<>();
        common.put("app_id", xfyunAppId);
        params.put("common", common);
        
        Map<String, Object> business = new HashMap<>();
        business.put("language", "zh_cn");
        business.put("domain", "iat");
        business.put("accent", "mandarin");
        business.put("vad_eos", 2000);
        business.put("dwa", "wpgs");
        params.put("business", business);
        
        Map<String, Object> data = new HashMap<>();
        data.put("status", 0); // 0表示第一帧
        data.put("format", "audio/L16;rate=16000");
        data.put("encoding", "raw");
        params.put("data", data);

        xfyunSession.getBasicRemote().sendText(objectMapper.writeValueAsString(params));
        log.info("已发送开始参数给科大讯飞");
    }

    /**
     * 发送结束标记给科大讯飞
     */
    private void sendEndToXFYun(Session xfyunSession) throws Exception {
        if (xfyunSession == null || !xfyunSession.isOpen()) {
            log.warn("科大讯飞连接未建立，无法发送结束标记");
            return;
        }

        Map<String, Object> endFrame = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("status", 2); // 2表示最后一帧
        data.put("format", "audio/L16;rate=16000");
        data.put("encoding", "raw");
        data.put("audio", "");
        endFrame.put("data", data);

        xfyunSession.getBasicRemote().sendText(objectMapper.writeValueAsString(endFrame));
        log.info("已发送结束标记给科大讯飞");
    }

    /**
     * 关闭科大讯飞连接
     */
    private void closeXFYunConnection(String sessionId) {
        Session xfyunSession = xfyunSessions.remove(sessionId);
        if (xfyunSession != null && xfyunSession.isOpen()) {
            try {
                xfyunSession.close();
                log.info("已关闭科大讯飞连接");
            } catch (Exception e) {
                log.error("关闭科大讯飞连接失败", e);
            }
        }
    }

    /**
     * 发送错误消息给前端
     */
    private void sendErrorToClient(WebSocketSession session, String errorMsg) {
        try {
            if (session.isOpen()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", errorMsg);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
            }
        } catch (Exception e) {
            log.error("发送错误消息失败", e);
        }
    }

    /**
     * 生成科大讯飞鉴权 URL
     */
    private String getAuthUrl() throws Exception {
        String url = "wss://iat-api.xfyun.cn/v2/iat";
        String host = "iat-api.xfyun.cn";
        
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = sdf.format(new Date());
        
        String signatureOrigin = "host: " + host + "\n" +
                                "date: " + date + "\n" +
                                "GET /v2/iat HTTP/1.1";
        
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec spec = new SecretKeySpec(xfyunApiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(spec);
        byte[] signatureBytes = mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signatureBytes);
        
        String authorization = String.format(
            "api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
            xfyunApiKey,
            "hmac-sha256",
            "host date request-line",
            signature
        );
        String authorizationBase64 = Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8));
        
        return url + "?authorization=" + authorizationBase64 +
               "&date=" + URLEncoder.encode(date, StandardCharsets.UTF_8) +
               "&host=" + host;
    }
}
