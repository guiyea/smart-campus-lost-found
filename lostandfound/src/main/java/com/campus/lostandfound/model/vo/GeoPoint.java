package com.campus.lostandfound.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地理坐标点VO
 */
@Schema(description = "地理坐标点")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoPoint {
    
    /**
     * 经度
     */
    @Schema(description = "经度", example = "121.4737", accessMode = Schema.AccessMode.READ_ONLY)
    private Double longitude;
    
    /**
     * 纬度
     */
    @Schema(description = "纬度", example = "31.2304", accessMode = Schema.AccessMode.READ_ONLY)
    private Double latitude;
    
    /**
     * 地址描述
     */
    @Schema(description = "地址描述", example = "上海市黄浦区南京东路", accessMode = Schema.AccessMode.READ_ONLY)
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
