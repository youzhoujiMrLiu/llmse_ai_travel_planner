package com.shingeki.travelplannerbackend.service;

import com.shingeki.travelplannerbackend.dto.CreateTravelPlanRequest;
import com.shingeki.travelplannerbackend.dto.TravelPlanDTO;
import com.shingeki.travelplannerbackend.dto.TravelPlanDetailDTO;
import com.shingeki.travelplannerbackend.entity.TravelPlan;
import com.shingeki.travelplannerbackend.repository.TravelPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 旅行计划服务类
 */
@Service
public class TravelPlanService {

    @Autowired
    private TravelPlanRepository travelPlanRepository;

    /**
     * 获取用户的所有旅行计划
     */
    public List<TravelPlanDTO> getUserTravelPlans(UUID userId) {
        List<TravelPlan> plans = travelPlanRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        // 自动更新状态
        plans.forEach(this::updatePlanStatus);
        
        return plans.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取旅行计划
     */
    public TravelPlanDTO getTravelPlanById(UUID planId, UUID userId) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("计划不存在"));
        
        // 验证是否是用户自己的计划
        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此计划");
        }
        
        // 自动更新状态
        updatePlanStatus(plan);
        
        return convertToDTO(plan);
    }

    /**
     * 创建旅行计划
     */
    @Transactional
    public TravelPlanDTO createTravelPlan(CreateTravelPlanRequest request, UUID userId) {
        TravelPlan plan = new TravelPlan();
        plan.setUserId(userId);
        plan.setDestination(request.getDestination());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setDuration(request.getDuration());
        plan.setBudget(request.getBudget());
        plan.setTravelers(request.getTravelers());
        
        if (request.getPreferences() != null && !request.getPreferences().isEmpty()) {
            plan.setPreferences(request.getPreferences().toArray(new String[0]));
        }
        
        plan.setUserInput(request.getUserInput());
        plan.setAiGeneratedPlan(request.getAiGeneratedPlan()); // 保存AI生成的详细计划
        plan.setStatus(determineInitialStatus(request.getStartDate()));
        
        TravelPlan savedPlan = travelPlanRepository.save(plan);
        return convertToDTO(savedPlan);
    }

    /**
     * 更新旅行计划
     */
    @Transactional
    public TravelPlanDTO updateTravelPlan(UUID planId, CreateTravelPlanRequest request, UUID userId) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("计划不存在"));
        
        // 验证权限
        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此计划");
        }
        
        plan.setDestination(request.getDestination());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setDuration(request.getDuration());
        plan.setBudget(request.getBudget());
        plan.setTravelers(request.getTravelers());
        
        if (request.getPreferences() != null && !request.getPreferences().isEmpty()) {
            plan.setPreferences(request.getPreferences().toArray(new String[0]));
        }
        
        plan.setUserInput(request.getUserInput());
        
        // 如果提供了新的AI计划,则更新
        if (request.getAiGeneratedPlan() != null) {
            plan.setAiGeneratedPlan(request.getAiGeneratedPlan());
        }
        
        TravelPlan savedPlan = travelPlanRepository.save(plan);
        updatePlanStatus(savedPlan);
        
        return convertToDTO(savedPlan);
    }

    /**
     * 删除旅行计划
     */
    @Transactional
    public void deleteTravelPlan(UUID planId, UUID userId) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("计划不存在"));
        
        // 验证权限
        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此计划");
        }
        
        travelPlanRepository.delete(plan);
    }

    /**
     * 手动标记计划为已完成
     */
    @Transactional
    public TravelPlanDTO markAsCompleted(UUID planId, UUID userId) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("计划不存在"));
        
        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此计划");
        }
        
        plan.setStatus("completed");
        TravelPlan savedPlan = travelPlanRepository.save(plan);
        
        return convertToDTO(savedPlan);
    }

    /**
     * 自动更新计划状态
     */
    private void updatePlanStatus(TravelPlan plan) {
        if ("completed".equals(plan.getStatus())) {
            return; // 已完成的不再更新
        }
        
        LocalDate today = LocalDate.now();
        String newStatus;
        
        if (today.isBefore(plan.getStartDate())) {
            newStatus = "planning";
        } else if (today.isAfter(plan.getEndDate())) {
            newStatus = "completed";
        } else {
            newStatus = "ongoing";
        }
        
        if (!newStatus.equals(plan.getStatus())) {
            plan.setStatus(newStatus);
            travelPlanRepository.save(plan);
        }
    }

    /**
     * 确定初始状态
     */
    private String determineInitialStatus(LocalDate startDate) {
        LocalDate today = LocalDate.now();
        
        if (today.isBefore(startDate)) {
            return "planning";
        } else {
            return "ongoing";
        }
    }

    /**
     * 获取旅行计划详情(包含AI生成的每日计划)
     */
    public TravelPlanDetailDTO getTravelPlanDetail(UUID planId, UUID userId) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("计划不存在"));
        
        // 验证权限
        if (!plan.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此计划");
        }
        
        return convertToDetailDTO(plan);
    }

    /**
     * 转换为 DTO
     */
    private TravelPlanDTO convertToDTO(TravelPlan plan) {
        TravelPlanDTO dto = new TravelPlanDTO();
        dto.setId(plan.getId());
        dto.setDestination(plan.getDestination());
        dto.setStartDate(plan.getStartDate());
        dto.setEndDate(plan.getEndDate());
        dto.setDuration(plan.getDuration());
        dto.setBudget(plan.getBudget());
        dto.setTravelers(plan.getTravelers());
        
        if (plan.getPreferences() != null) {
            dto.setPreferences(Arrays.asList(plan.getPreferences()));
        }
        
        dto.setStatus(plan.getStatus());
        dto.setCreatedAt(plan.getCreatedAt());
        dto.setUpdatedAt(plan.getUpdatedAt());
        
        return dto;
    }

    /**
     * 转换为详细 DTO
     */
    private TravelPlanDetailDTO convertToDetailDTO(TravelPlan plan) {
        TravelPlanDetailDTO dto = new TravelPlanDetailDTO();
        dto.setId(plan.getId());
        dto.setDestination(plan.getDestination());
        dto.setStartDate(plan.getStartDate());
        dto.setEndDate(plan.getEndDate());
        dto.setDuration(plan.getDuration());
        dto.setBudget(plan.getBudget());
        dto.setTravelers(plan.getTravelers());
        
        if (plan.getPreferences() != null) {
            dto.setPreferences(Arrays.asList(plan.getPreferences()));
        }
        
        dto.setStatus(plan.getStatus());
        dto.setAiGeneratedPlan(plan.getAiGeneratedPlan());
        dto.setCreatedAt(plan.getCreatedAt());
        dto.setUpdatedAt(plan.getUpdatedAt());
        
        return dto;
    }
}
