package com.shingeki.travelplannerbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shingeki.travelplannerbackend.dto.GeneratePlanRequest;
import com.shingeki.travelplannerbackend.dto.GeneratedPlanResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

/**
 * AI 服务 - 调用通义千问 API 生成旅行计划
 */
@Service
public class AIService {

    @Value("${qwen.api.key}")
    private String qwenApiKey;

    @Value("${qwen.api.url}")
    private String qwenApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 构造函数注入,配置超时时间
    public AIService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))  // 连接超时 10 秒
                .setReadTimeout(Duration.ofSeconds(120))    // 读取超时 120 秒(AI 生成需要较长时间)
                .build();
    }

    /**
     * 生成旅行计划
     */
    public GeneratedPlanResponse generateTravelPlan(GeneratePlanRequest request) {
        try {
            // 构建提示词
            String prompt = buildPrompt(request);
            
            // 调用通义千问 API
            String aiResponse = callQwenAPI(prompt);
            
            // 解析 AI 响应
            GeneratedPlanResponse response = parseAIResponse(aiResponse, request);
            
            return response;
        } catch (Exception e) {
            System.err.println("AI 生成计划失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("AI 生成计划失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(GeneratePlanRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的旅行规划师。请根据以下用户需求，生成一份详细的旅行计划。\n\n");
        prompt.append("用户需求：").append(request.getUserInput()).append("\n\n");
        prompt.append("具体信息：\n");
        prompt.append("- 目的地：").append(request.getDestination()).append("\n");
        prompt.append("- 天数：").append(request.getDuration()).append("天\n");
        prompt.append("- 预算：").append(request.getBudget()).append("元\n");
        prompt.append("- 人数：").append(request.getTravelers()).append("人\n");
        
        if (request.getPreferences() != null && !request.getPreferences().isEmpty()) {
            prompt.append("- 偏好：").append(request.getPreferences()).append("\n");
        }
        
        prompt.append("\n请以 JSON 格式返回旅行计划，格式如下：\n");
        prompt.append("{\n");
        prompt.append("  \"summary\": \"行程概述\",\n");
        prompt.append("  \"dailyPlans\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"day\": 1,\n");
        prompt.append("      \"activities\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"time\": \"09:00-12:00\",\n");
        prompt.append("          \"type\": \"attraction\",\n");
        prompt.append("          \"title\": \"景点名称\",\n");
        prompt.append("          \"description\": \"详细描述\",\n");
        prompt.append("          \"location\": \"具体地址\",\n");
        prompt.append("          \"estimatedCost\": 100.0\n");
        prompt.append("        }\n");
        prompt.append("      ]\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"budgetBreakdown\": {\n");
        prompt.append("    \"accommodation\": 1000.0,\n");
        prompt.append("    \"food\": 800.0,\n");
        prompt.append("    \"transport\": 500.0,\n");
        prompt.append("    \"attraction\": 600.0,\n");
        prompt.append("    \"shopping\": 100.0,\n");
        prompt.append("    \"other\": 0.0\n");
        prompt.append("  },\n");
        prompt.append("  \"tips\": [\"旅行建议1\", \"旅行建议2\"]\n");
        prompt.append("}\n\n");
        prompt.append("注意：\n");
        prompt.append("1. type 可选值：transport, attraction, restaurant, accommodation\n");
        prompt.append("2. 确保预算分配合理，总和不超过用户预算\n");
        prompt.append("3. 活动安排要符合实际时间和地理位置\n");
        prompt.append("4. 只返回 JSON，不要有其他文字说明\n");
        
        return prompt.toString();
    }

    /**
     * 调用通义千问 API
     */
    private String callQwenAPI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + qwenApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen-plus-2025-09-11");
            
            Map<String, Object> input = new HashMap<>();
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            
            input.put("messages", messages);
            requestBody.put("input", input);
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("result_format", "message");
            requestBody.put("parameters", parameters);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                qwenApiUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("output").path("choices").get(0)
                        .path("message").path("content").asText();
            } else {
                throw new RuntimeException("API 调用失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("调用通义千问 API 失败: " + e.getMessage());
            throw new RuntimeException("调用 AI API 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析 AI 响应
     */
    private GeneratedPlanResponse parseAIResponse(String aiResponse, GeneratePlanRequest request) {
        try {
            // 提取 JSON 部分（移除可能的 markdown 代码块标记）
            String jsonString = aiResponse.trim();
            if (jsonString.startsWith("```json")) {
                jsonString = jsonString.substring(7);
            }
            if (jsonString.startsWith("```")) {
                jsonString = jsonString.substring(3);
            }
            if (jsonString.endsWith("```")) {
                jsonString = jsonString.substring(0, jsonString.length() - 3);
            }
            jsonString = jsonString.trim();

            GeneratedPlanResponse response = objectMapper.readValue(jsonString, GeneratedPlanResponse.class);
            response.setDestination(request.getDestination());
            
            return response;
        } catch (Exception e) {
            System.err.println("解析 AI 响应失败: " + e.getMessage());
            System.err.println("AI 响应内容: " + aiResponse);
            
            // 返回一个简单的默认响应
            return createFallbackResponse(request);
        }
    }

    /**
     * 创建降级响应（当 AI 解析失败时）
     */
    private GeneratedPlanResponse createFallbackResponse(GeneratePlanRequest request) {
        GeneratedPlanResponse response = new GeneratedPlanResponse();
        response.setDestination(request.getDestination());
        response.setSummary("由于 AI 服务暂时不可用，这是一个基础的旅行计划模板。");
        
        List<GeneratedPlanResponse.DayPlan> dailyPlans = new ArrayList<>();
        for (int i = 1; i <= request.getDuration(); i++) {
            GeneratedPlanResponse.DayPlan dayPlan = new GeneratedPlanResponse.DayPlan();
            dayPlan.setDay(i);
            
            List<GeneratedPlanResponse.Activity> activities = new ArrayList<>();
            
            GeneratedPlanResponse.Activity activity = new GeneratedPlanResponse.Activity();
            activity.setTime("09:00-18:00");
            activity.setType("attraction");
            activity.setTitle("探索" + request.getDestination());
            activity.setDescription("自由活动时间，探索当地景点");
            activity.setLocation(request.getDestination());
            activity.setEstimatedCost(request.getBudget().doubleValue() / request.getDuration());
            
            activities.add(activity);
            dayPlan.setActivities(activities);
            dailyPlans.add(dayPlan);
        }
        
        response.setDailyPlans(dailyPlans);
        
        GeneratedPlanResponse.BudgetBreakdown budget = new GeneratedPlanResponse.BudgetBreakdown();
        double total = request.getBudget().doubleValue();
        budget.setAccommodation(total * 0.3);
        budget.setFood(total * 0.25);
        budget.setTransport(total * 0.15);
        budget.setAttraction(total * 0.2);
        budget.setShopping(total * 0.1);
        budget.setOther(0.0);
        response.setBudgetBreakdown(budget);
        
        response.setTips(Arrays.asList(
            "建议提前预订住宿",
            "注意当地天气情况",
            "保管好个人财物"
        ));
        
        return response;
    }
}
