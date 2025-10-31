package com.shingeki.travelplannerbackend.repository;

import com.shingeki.travelplannerbackend.entity.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 旅行计划仓库接口
 */
@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlan, UUID> {
    
    /**
     * 根据用户ID查询所有旅行计划，按创建时间倒序排列
     */
    List<TravelPlan> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * 根据用户ID和状态查询旅行计划
     */
    List<TravelPlan> findByUserIdAndStatus(UUID userId, String status);
}
