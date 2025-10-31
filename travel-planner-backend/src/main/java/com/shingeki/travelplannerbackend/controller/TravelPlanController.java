package com.shingeki.travelplannerbackend.controller;

import com.shingeki.travelplannerbackend.dto.CreateTravelPlanRequest;
import com.shingeki.travelplannerbackend.dto.TravelPlanDTO;
import com.shingeki.travelplannerbackend.security.SupabaseJwtValidator;
import com.shingeki.travelplannerbackend.service.TravelPlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 旅行计划控制器
 */
@RestController
@RequestMapping("/api/travel-plans")
@CrossOrigin(origins = "*")
public class TravelPlanController {

    @Autowired
    private TravelPlanService travelPlanService;

    @Autowired
    private SupabaseJwtValidator jwtValidator;

    /**
     * 获取当前用户的所有旅行计划
     */
    @GetMapping
    public ResponseEntity<List<TravelPlanDTO>> getUserTravelPlans(
            @RequestHeader("Authorization") String authHeader) {
        UUID userId = jwtValidator.validateTokenAndGetUserId(authHeader);
        List<TravelPlanDTO> plans = travelPlanService.getUserTravelPlans(userId);
        return ResponseEntity.ok(plans);
    }

    /**
     * 根据ID获取旅行计划详情
     */
    @GetMapping("/{planId}")
    public ResponseEntity<TravelPlanDTO> getTravelPlanById(
            @PathVariable UUID planId,
            @RequestHeader("Authorization") String authHeader) {
        UUID userId = jwtValidator.validateTokenAndGetUserId(authHeader);
        TravelPlanDTO plan = travelPlanService.getTravelPlanById(planId, userId);
        return ResponseEntity.ok(plan);
    }

    /**
     * 创建旅行计划
     */
    @PostMapping
    public ResponseEntity<TravelPlanDTO> createTravelPlan(
            @Valid @RequestBody CreateTravelPlanRequest request,
            @RequestHeader("Authorization") String authHeader) {
        UUID userId = jwtValidator.validateTokenAndGetUserId(authHeader);
        TravelPlanDTO plan = travelPlanService.createTravelPlan(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(plan);
    }

    /**
     * 更新旅行计划
     */
    @PutMapping("/{planId}")
    public ResponseEntity<TravelPlanDTO> updateTravelPlan(
            @PathVariable UUID planId,
            @Valid @RequestBody CreateTravelPlanRequest request,
            @RequestHeader("Authorization") String authHeader) {
        UUID userId = jwtValidator.validateTokenAndGetUserId(authHeader);
        TravelPlanDTO plan = travelPlanService.updateTravelPlan(planId, request, userId);
        return ResponseEntity.ok(plan);
    }

    /**
     * 删除旅行计划
     */
    @DeleteMapping("/{planId}")
    public ResponseEntity<Map<String, String>> deleteTravelPlan(
            @PathVariable UUID planId,
            @RequestHeader("Authorization") String authHeader) {
        UUID userId = jwtValidator.validateTokenAndGetUserId(authHeader);
        travelPlanService.deleteTravelPlan(planId, userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "计划已删除");
        return ResponseEntity.ok(response);
    }

    /**
     * 手动标记计划为已完成
     */
    @PatchMapping("/{planId}/complete")
    public ResponseEntity<TravelPlanDTO> markAsCompleted(
            @PathVariable UUID planId,
            @RequestHeader("Authorization") String authHeader) {
        UUID userId = jwtValidator.validateTokenAndGetUserId(authHeader);
        TravelPlanDTO plan = travelPlanService.markAsCompleted(planId, userId);
        return ResponseEntity.ok(plan);
    }

    /**
     * 全局异常处理
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
