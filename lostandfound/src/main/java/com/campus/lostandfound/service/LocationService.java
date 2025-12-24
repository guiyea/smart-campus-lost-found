package com.campus.lostandfound.service;

import com.campus.lostandfound.model.vo.GeoPoint;

import java.util.List;

/**
 * 地理位置服务接口
 * 提供地理编码、逆地理编码、距离计算和范围查询功能
 */
public interface LocationService {
    
    /**
     * 地址转坐标（地理编码）
     * 将详细的结构化地址转换为经纬度坐标
     *
     * @param address 待解析的地址（如：北京市朝阳区阜通东大街6号）
     * @return 包含经纬度和地址的GeoPoint对象，解析失败返回null
     */
    GeoPoint geocode(String address);
    
    /**
     * 坐标转地址（逆地理编码）
     * 将经纬度坐标转换为详细的结构化地址
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 格式化的地址字符串，解析失败返回null
     */
    String reverseGeocode(Double lng, Double lat);
    
    /**
     * 计算两点之间的距离（米）
     * 使用Haversine公式计算球面距离
     *
     * @param p1 第一个坐标点
     * @param p2 第二个坐标点
     * @return 两点之间的距离（米）
     */
    Double calculateDistance(GeoPoint p1, GeoPoint p2);
    
    /**
     * 范围查询物品ID
     * 查询指定坐标点周围指定半径范围内的物品ID列表
     *
     * @param lng    中心点经度
     * @param lat    中心点纬度
     * @param radius 搜索半径（米）
     * @return 范围内的物品ID列表
     */
    List<Long> searchInRadius(Double lng, Double lat, Integer radius);
}
