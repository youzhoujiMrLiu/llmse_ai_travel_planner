package com.shingeki.travelplannerbackend.dto;

import java.util.List;

/**
 * AI 生成的旅行计划响应 DTO
 */
public class GeneratedPlanResponse {
    
    private String destination;
    private String summary;
    private List<DayPlan> dailyPlans;
    private BudgetBreakdown budgetBreakdown;
    private List<String> tips;

    public static class DayPlan {
        private Integer day;
        private String date;
        private List<Activity> activities;

        // Getters and Setters
        public Integer getDay() {
            return day;
        }

        public void setDay(Integer day) {
            this.day = day;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public List<Activity> getActivities() {
            return activities;
        }

        public void setActivities(List<Activity> activities) {
            this.activities = activities;
        }
    }

    public static class Activity {
        private String time;
        private String type; // transport, attraction, restaurant, accommodation
        private String title;
        private String description;
        private String location;
        private Double estimatedCost;
        private LocationCoordinate coordinate;

        // Getters and Setters
        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public Double getEstimatedCost() {
            return estimatedCost;
        }

        public void setEstimatedCost(Double estimatedCost) {
            this.estimatedCost = estimatedCost;
        }

        public LocationCoordinate getCoordinate() {
            return coordinate;
        }

        public void setCoordinate(LocationCoordinate coordinate) {
            this.coordinate = coordinate;
        }
    }

    public static class LocationCoordinate {
        private Double latitude;
        private Double longitude;

        public LocationCoordinate() {
        }

        public LocationCoordinate(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        // Getters and Setters
        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }

    public static class BudgetBreakdown {
        private Double accommodation;
        private Double food;
        private Double transport;
        private Double attraction;
        private Double shopping;
        private Double other;

        // Getters and Setters
        public Double getAccommodation() {
            return accommodation;
        }

        public void setAccommodation(Double accommodation) {
            this.accommodation = accommodation;
        }

        public Double getFood() {
            return food;
        }

        public void setFood(Double food) {
            this.food = food;
        }

        public Double getTransport() {
            return transport;
        }

        public void setTransport(Double transport) {
            this.transport = transport;
        }

        public Double getAttraction() {
            return attraction;
        }

        public void setAttraction(Double attraction) {
            this.attraction = attraction;
        }

        public Double getShopping() {
            return shopping;
        }

        public void setShopping(Double shopping) {
            this.shopping = shopping;
        }

        public Double getOther() {
            return other;
        }

        public void setOther(Double other) {
            this.other = other;
        }
    }

    // Getters and Setters
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<DayPlan> getDailyPlans() {
        return dailyPlans;
    }

    public void setDailyPlans(List<DayPlan> dailyPlans) {
        this.dailyPlans = dailyPlans;
    }

    public BudgetBreakdown getBudgetBreakdown() {
        return budgetBreakdown;
    }

    public void setBudgetBreakdown(BudgetBreakdown budgetBreakdown) {
        this.budgetBreakdown = budgetBreakdown;
    }

    public List<String> getTips() {
        return tips;
    }

    public void setTips(List<String> tips) {
        this.tips = tips;
    }
}
