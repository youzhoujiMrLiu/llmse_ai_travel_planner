package com.shingeki.travelplannerbackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 地图服务控制器 - 代理高德地图 Web 服务 API
 */
@RestController
@RequestMapping("/api/map")
@CrossOrigin(origins = "*")
public class MapController {

    @Value("${amap.api.key}")
    private String amapApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 地点搜索接口（POI搜索）
     * @param keywords 搜索关键词
     * @param city 城市（可选）
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchPlace(
            @RequestParam String keywords,
            @RequestParam(required = false) String city
    ) {
        try {
            // 构建请求 URL
            String url = "https://restapi.amap.com/v3/place/text?key=" + amapApiKey
                    + "&keywords=" + keywords
                    + "&types="
                    + "&city=" + (city != null ? city : "")
                    + "&offset=1"
                    + "&page=1"
                    + "&extensions=base";

            // 调用高德 Web 服务 API
            String response = restTemplate.getForObject(url, String.class);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "地点搜索失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 地理编码接口（地址转坐标）
     * @param address 地址
     * @return 坐标信息
     */
    @GetMapping("/geocode")
    public ResponseEntity<?> geocode(@RequestParam String address) {
        try {
            String url = "https://restapi.amap.com/v3/geocode/geo?key=" + amapApiKey
                    + "&address=" + address;

            String response = restTemplate.getForObject(url, String.class);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "地理编码失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
