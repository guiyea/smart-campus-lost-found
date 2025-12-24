package com.campus.lostandfound.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地理坐标点VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoPoint {
    
    /**
     * 经度
     */
    private Double longitude;
    
    /**
     * 纬度
     */
    private Double latitude;
    
    /**
     * 地址描述
     */
    private String address;
    
    /**
     * 创建只包含坐标的GeoPoint
     */
    public static GeoPoint of(Double longitude, Double latitude) {
        return GeoPoint.builder()
                .longitude(longitude)
                .latitude(latitude)
                .build();
    }
    
    /**
     * 创建包含坐标和地址的GeoPoint
     */
    public static GeoPoint of(Double longitude, Double latitude, String address) {
        return GeoPoint.builder()
                .longitude(longitude)
                .latitude(latitude)
                .address(address)
                .build();
    }
}
