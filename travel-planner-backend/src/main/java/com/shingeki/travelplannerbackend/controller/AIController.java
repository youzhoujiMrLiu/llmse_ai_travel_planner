package com.shingeki.travelplannerbackend.controller;

import com.shingeki.travelplannerbackend.dto.GeneratePlanRequest;
import com.shingeki.travelplannerbackend.dto.GeneratedPlanResponse;
import com.shingeki.travelplannerbackend.security.SupabaseJwtValidator;
import com.shingeki.travelplannerbackend.service.AIService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 控制器 - 生成旅行计划
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private SupabaseJwtValidator jwtValidator;

    /**
     * 智能解析用户输入
     */
    @PostMapping("/parse-input")
    public ResponseEntity<Map<String, Object>> parseUserInput(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        
        // 验证用户身份
        jwtValidator.validateTokenAndGetUserId(authHeader);
        
        String userInput = request.get("userInput");
        
        // 调用 AI 服务解析用户输入
        Map<String, Object> parsedData = aiService.parseUserInput(userInput);
        
        return ResponseEntity.ok(parsedData);
    }

    /**
     * 生成旅行计划
     */
    @PostMapping("/generate-plan")
    public ResponseEntity<GeneratedPlanResponse> generatePlan(
            @Valid @RequestBody GeneratePlanRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        // 验证用户身份
        jwtValidator.validateTokenAndGetUserId(authHeader);
        
        // 调用 AI 服务生成计划
        GeneratedPlanResponse response = aiService.generateTravelPlan(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 全局异常处理
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
}
