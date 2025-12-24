package com.campus.lostandfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostandfound.config.AmapProperties;
import com.campus.lostandfound.model.entity.Item;
import com.campus.lostandfound.model.vo.GeoPoint;
import com.campus.lostandfound.repository.ItemMapper;
import com.campus.lostandfound.service.LocationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 地理位置服务实现类
 * 集成高德地图Web服务API实现地理编码、逆地理编码等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    
    private final AmapProperties amapProperties;
    
    @Qualifier("amapRestTemplate")
    private final RestTemplate amapRestTemplate;
    
    private final ItemMapper itemMapper;
    
    private final ObjectMapper objectMapper;
    
    /**
     * 地球半径（米）
     */
    private static final double EARTH_RADIUS = 6371000;
    
    /**
     * 高德API成功状态码
     */
    private static final String AMAP_SUCCESS_STATUS = "1";
    
    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_ATTEMPTS = 2;
    
    /**
     * 重试间隔（毫秒）
     */
    private static final long RETRY_DELAY_MS = 500;
    
    @Override
    public GeoPoint geocode(String address) {
        if (address == null || address.trim().isEmpty()) {
            log.warn("地理编码失败：地址为空");
            return null;
        }
        
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts <= MAX_RETRY_ATTEMPTS) {
            try {
                return doGeocode(address);
            } catch (RestClientException e) {
                lastException = e;
                attempts++;
                log.warn("地理编码API调用失败，第{}次重试，地址={}, 错误={}", attempts, address, e.getMessage());
                if (attempts <= MAX_RETRY_ATTEMPTS) {
                    sleep(RETRY_DELAY_MS);
                }
            } catch (Exception e) {
                log.error("地理编码解析失败：地址={}, 错误={}", address, e.getMessage(), e);
                return null;
            }
        }
        
        log.error("地理编码失败，已达最大重试次数：地址={}, 最后错误={}", address, 
                lastException != null ? lastException.getMessage() : "unknown");
        return null;
    }
    
    private GeoPoint doGeocode(String address) throws RestClientException {
        String url = String.format("%s/geocode/geo?key=%s&address=%s&output=JSON",
                amapProperties.getBaseUrl(),
                amapProperties.getKey(),
                address);
        
        log.debug("调用高德地理编码API: {}", url.replace(amapProperties.getKey(), "***"));
        
        ResponseEntity<String> response = amapRestTemplate.getForEntity(url, String.class);
        
        if (response.getBody() == null) {
            log.warn("地理编码失败：响应为空，地址={}", address);
            return null;
        }
        
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            String status = root.path("status").asText();

            if (!AMAP_SUCCESS_STATUS.equals(status)) {
                String info = root.path("info").asText();
                log.warn("地理编码失败：status={}, info={}, 地址={}", status, info, address);
                return null;
            }
            
            JsonNode geocodes = root.path("geocodes");
            if (geocodes.isEmpty() || !geocodes.isArray() || geocodes.size() == 0) {
                log.warn("地理编码失败：未找到匹配结果，地址={}", address);
                return null;
            }
            
            JsonNode firstResult = geocodes.get(0);
            String location = firstResult.path("location").asText();
            String formattedAddress = firstResult.path("formatted_address").asText();
            
            if (location == null || location.isEmpty()) {
                log.warn("地理编码失败：坐标为空，地址={}", address);
                return null;
            }
            
            String[] coords = location.split(",");
            if (coords.length != 2) {
                log.warn("地理编码失败：坐标格式错误，location={}", location);
                return null;
            }
            
            Double lng = Double.parseDouble(coords[0]);
            Double lat = Double.parseDouble(coords[1]);
            
            log.info("地理编码成功：地址={} -> 坐标=({}, {})", address, lng, lat);
            
            return GeoPoint.of(lng, lat, formattedAddress);
            
        } catch (Exception e) {
            log.error("地理编码JSON解析失败：地址={}, 错误={}", address, e.getMessage());
            return null;
        }
    }
    
    @Override
    public String reverseGeocode(Double lng, Double lat) {
        if (lng == null || lat == null) {
            log.warn("逆地理编码失败：坐标为空");
            return null;
        }
        
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts <= MAX_RETRY_ATTEMPTS) {
            try {
                return doReverseGeocode(lng, lat);
            } catch (RestClientException e) {
                lastException = e;
                attempts++;
                log.warn("逆地理编码API调用失败，第{}次重试，坐标=({}, {}), 错误={}", attempts, lng, lat, e.getMessage());
                if (attempts <= MAX_RETRY_ATTEMPTS) {
                    sleep(RETRY_DELAY_MS);
                }
            } catch (Exception e) {
                log.error("逆地理编码解析失败：坐标=({}, {}), 错误={}", lng, lat, e.getMessage(), e);
                return null;
            }
        }
        
        log.error("逆地理编码失败，已达最大重试次数：坐标=({}, {}), 最后错误={}", lng, lat,
                lastException != null ? lastException.getMessage() : "unknown");
        return null;
    }
    
    private String doReverseGeocode(Double lng, Double lat) throws RestClientException {
        String url = String.format("%s/geocode/regeo?key=%s&location=%s,%s&output=JSON&extensions=base",
                amapProperties.getBaseUrl(),
                amapProperties.getKey(),
                lng,
                lat);
        
        log.debug("调用高德逆地理编码API: {}", url.replace(amapProperties.getKey(), "***"));
        
        ResponseEntity<String> response = amapRestTemplate.getForEntity(url, String.class);
        
        if (response.getBody() == null) {
            log.warn("逆地理编码失败：响应为空，坐标=({}, {})", lng, lat);
            return null;
        }
        
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            String status = root.path("status").asText();
            
            if (!AMAP_SUCCESS_STATUS.equals(status)) {
                String info = root.path("info").asText();
                log.warn("逆地理编码失败：status={}, info={}, 坐标=({}, {})", status, info, lng, lat);
                return null;
            }
            
            JsonNode regeocode = root.path("regeocode");
            if (regeocode.isMissingNode()) {
                log.warn("逆地理编码失败：未找到结果，坐标=({}, {})", lng, lat);
                return null;
            }
            
            String formattedAddress = regeocode.path("formatted_address").asText();
            
            if (formattedAddress == null || formattedAddress.isEmpty() || "[]".equals(formattedAddress)) {
                log.warn("逆地理编码失败：地址为空，坐标=({}, {})", lng, lat);
                return null;
            }
            
            log.info("逆地理编码成功：坐标=({}, {}) -> 地址={}", lng, lat, formattedAddress);
            
            return formattedAddress;
            
        } catch (Exception e) {
            log.error("逆地理编码JSON解析失败：坐标=({}, {}), 错误={}", lng, lat, e.getMessage());
            return null;
        }
    }

    
    @Override
    public Double calculateDistance(GeoPoint p1, GeoPoint p2) {
        if (p1 == null || p2 == null) {
            log.warn("距离计算失败：坐标点为空");
            return null;
        }
        
        if (p1.getLongitude() == null || p1.getLatitude() == null ||
            p2.getLongitude() == null || p2.getLatitude() == null) {
            log.warn("距离计算失败：坐标值为空");
            return null;
        }
        
        // 使用Haversine公式计算球面距离
        double lat1 = Math.toRadians(p1.getLatitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double lng1 = Math.toRadians(p1.getLongitude());
        double lng2 = Math.toRadians(p2.getLongitude());
        
        double dLat = lat2 - lat1;
        double dLng = lng2 - lng1;
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        double distance = EARTH_RADIUS * c;
        
        log.debug("距离计算：({}, {}) -> ({}, {}) = {}米",
                p1.getLongitude(), p1.getLatitude(),
                p2.getLongitude(), p2.getLatitude(),
                distance);
        
        return distance;
    }
    
    @Override
    public List<Long> searchInRadius(Double lng, Double lat, Integer radius) {
        if (lng == null || lat == null || radius == null || radius <= 0) {
            log.warn("范围查询失败：参数无效，lng={}, lat={}, radius={}", lng, lat, radius);
            return new ArrayList<>();
        }
        
        log.debug("执行范围查询：中心点=({}, {}), 半径={}米", lng, lat, radius);
        
        // 计算经纬度范围（粗略筛选，减少计算量）
        // 纬度1度约111km，经度1度约111km*cos(纬度)
        double latDelta = radius / 111000.0;
        double lngDelta = radius / (111000.0 * Math.cos(Math.toRadians(lat)));
        
        double minLat = lat - latDelta;
        double maxLat = lat + latDelta;
        double minLng = lng - lngDelta;
        double maxLng = lng + lngDelta;
        
        // 先用矩形范围粗略筛选
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Item::getLatitude, BigDecimal.valueOf(minLat))
                    .le(Item::getLatitude, BigDecimal.valueOf(maxLat))
                    .ge(Item::getLongitude, BigDecimal.valueOf(minLng))
                    .le(Item::getLongitude, BigDecimal.valueOf(maxLng))
                    .eq(Item::getDeleted, 0)
                    .isNotNull(Item::getLongitude)
                    .isNotNull(Item::getLatitude);
        
        List<Item> candidateItems = itemMapper.selectList(queryWrapper);
        
        if (candidateItems.isEmpty()) {
            log.debug("范围查询：矩形范围内无物品");
            return new ArrayList<>();
        }
        
        // 精确计算距离，过滤出圆形范围内的物品
        GeoPoint center = GeoPoint.of(lng, lat);
        
        List<Long> resultIds = candidateItems.stream()
                .filter(item -> {
                    GeoPoint itemPoint = GeoPoint.of(
                            item.getLongitude().doubleValue(),
                            item.getLatitude().doubleValue()
                    );
                    Double distance = calculateDistance(center, itemPoint);
                    return distance != null && distance <= radius;
                })
                .map(Item::getId)
                .collect(Collectors.toList());
        
        log.info("范围查询完成：中心点=({}, {}), 半径={}米, 候选数={}, 结果数={}",
                lng, lat, radius, candidateItems.size(), resultIds.size());
        
        return resultIds;
    }
    
    /**
     * 线程休眠
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
