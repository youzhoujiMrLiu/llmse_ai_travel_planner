package com.shingeki.travelplannerbackend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * AI 生成旅行计划请求 DTO
 */
public class GeneratePlanRequest {

    @NotBlank(message = "用户输入不能为空")
    private String userInput;

    @NotBlank(message = "目的地不能为空")
    private String destination;

    @NotNull(message = "天数不能为空")
    @Min(value = 1, message = "天数至少为1天")
    private Integer duration;

    @NotNull(message = "预算不能为空")
    private BigDecimal budget;

    @NotNull(message = "同行人数不能为空")
    @Min(value = 1, message = "同行人数至少为1人")
    private Integer travelers;

    private String preferences; // 偏好，例如："美食,动漫,购物"
    
    private String additionalRequirements; // 额外要求，例如："预算主要用于美食，住宿标准要高一些"

    // Getters and Setters
    public String getUserInput() {
        return userInput;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public Integer getTravelers() {
        return travelers;
    }

    public void setTravelers(Integer travelers) {
        this.travelers = travelers;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }
    
    public String getAdditionalRequirements() {
        return additionalRequirements;
    }
    
    public void setAdditionalRequirements(String additionalRequirements) {
        this.additionalRequirements = additionalRequirements;
    }
}
