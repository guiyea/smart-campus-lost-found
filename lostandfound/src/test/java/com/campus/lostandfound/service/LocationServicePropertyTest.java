package com.campus.lostandfound.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostandfound.config.AmapProperties;
import com.campus.lostandfound.model.entity.Item;
import com.campus.lostandfound.model.vo.GeoPoint;
import com.campus.lostandfound.repository.ItemMapper;
import com.campus.lostandfound.service.impl.LocationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 地理位置服务属性测试
 * 
 * Feature: smart-campus-lost-found, Property 8: 地理范围查询准确性
 * Validates: Requirements 4.3
 */
class LocationServicePropertyTest {

    /**
     * 地球半径（米）- 用于距离计算验证
     */
    private static final double EARTH_RADIUS = 6371000;

    /**
     * Property 8: 地理范围查询准确性 - 返回的所有物品距离中心点应<=radius
     * 
     * Feature: smart-campus-lost-found, Property 8: 地理范围查询准确性
     * 测试: 对于任意中心点和半径，返回的所有物品距离中心点应<=radius
     * Validates: Requirements 4.3
     */
    @Property(tries = 100)
    void allReturnedItemsShouldBeWithinRadius(
            @ForAll("validCenterPoint") GeoTestPoint center,
            @ForAll @IntRange(min = 100, max = 10000) int radius,
            @ForAll("itemsAroundCenter") List<ItemWithDistance> itemsWithDistances) {
        
        // Setup mocks
        AmapProperties amapProperties = mock(AmapProperties.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ItemMapper itemMapper = mock(ItemMapper.class);
        ObjectMapper objectMapper = new ObjectMapper();
        
        when(amapProperties.getBaseUrl()).thenReturn("https://restapi.amap.com/v3");
        when(amapProperties.getKey()).thenReturn("test-key");
        
        LocationServiceImpl locationService = new LocationServiceImpl(
                amapProperties, restTemplate, itemMapper, objectMapper);
        
        // Generate items at various distances from center
        List<Item> allItems = itemsWithDistances.stream()
                .map(iwd -> createItemAtDistance(center, iwd, radius))
                .collect(Collectors.toList());
        
        // Mock the database query to return all items in the rectangular bounding box
        when(itemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(allItems);
        
        // Execute the search
        List<Long> resultIds = locationService.searchInRadius(
                center.longitude, center.latitude, radius);
        
        // Verify: all returned items should be within radius
        for (Long itemId : resultIds) {
            Item item = allItems.stream()
                    .filter(i -> i.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);
            
            assertNotNull(item, "Returned item ID should exist in the item list");
            
            double actualDistance = calculateHaversineDistance(
                    center.longitude, center.latitude,
                    item.getLongitude().doubleValue(), item.getLatitude().doubleValue());
            
            assertTrue(actualDistance <= radius,
                    String.format("Item %d at distance %.2f meters should be within radius %d meters",
                            itemId, actualDistance, radius));
        }
    }

    /**
     * Property 8: 地理范围查询准确性 - 范围外的物品不应出现在结果中
     * 
     * Feature: smart-campus-lost-found, Property 8: 地理范围查询准确性
     * 测试: 对于任意中心点和半径，范围外的物品不应出现在结果中
     * Validates: Requirements 4.3
     */
    @Property(tries = 100)
    void itemsOutsideRadiusShouldNotAppearInResults(
            @ForAll("validCenterPoint") GeoTestPoint center,
            @ForAll @IntRange(min = 100, max = 5000) int radius,
            @ForAll("mixedDistanceItems") List<ItemWithDistance> itemsWithDistances) {
        
        // Setup mocks
        AmapProperties amapProperties = mock(AmapProperties.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        ItemMapper itemMapper = mock(ItemMapper.class);
        ObjectMapper objectMapper = new ObjectMapper();
        
        when(amapProperties.getBaseUrl()).thenReturn("https://restapi.amap.com/v3");
        when(amapProperties.getKey()).thenReturn("test-key");
        
        LocationServiceImpl locationService = new LocationServiceImpl(
                amapProperties, restTemplate, itemMapper, objectMapper);
        
        // Create items - some inside, some outside the radius
        List<Item> allItems = new ArrayList<>();
        List<Long> expectedOutsideIds = new ArrayList<>();
        
        for (int i = 0; i < itemsWithDistances.size(); i++) {
            ItemWithDistance iwd = itemsWithDistances.get(i);
            Item item = createItemAtExactDistance(center, iwd.distanceMeters, (long) (i + 1));
            allItems.add(item);
            
            // Track items that should be outside the radius
            if (iwd.distanceMeters > radius) {
                expectedOutsideIds.add(item.getId());
            }
        }
        
        // Mock the database query
        when(itemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(allItems);
        
        // Execute the search
        List<Long> resultIds = locationService.searchInRadius(
                center.longitude, center.latitude, radius);
        
        // Verify: items outside radius should NOT be in results
        for (Long outsideId : expectedOutsideIds) {
            assertFalse(resultIds.contains(outsideId),
                    String.format("Item %d outside radius should not appear in results", outsideId));
        }
    }

    // ==================== Test Data Classes ====================

    /**
     * Test data holder for geographic point
     */
    static class GeoTestPoint {
        double longitude;
        double latitude;
        
        GeoTestPoint(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }
        
        @Override
        public String toString() {
            return String.format("GeoTestPoint{lng=%.6f, lat=%.6f}", longitude, latitude);
        }
    }

    /**
     * Test data holder for item with distance information
     */
    static class ItemWithDistance {
        double distanceMeters;
        double bearingDegrees;
        
        ItemWithDistance(double distanceMeters, double bearingDegrees) {
            this.distanceMeters = distanceMeters;
            this.bearingDegrees = bearingDegrees;
        }
        
        @Override
        public String toString() {
            return String.format("ItemWithDistance{distance=%.2f, bearing=%.2f}", 
                    distanceMeters, bearingDegrees);
        }
    }

    // ==================== Generators ====================

    /**
     * Generator for valid center points (within China's approximate bounds)
     */
    @Provide
    Arbitrary<GeoTestPoint> validCenterPoint() {
        // China's approximate bounds: lng 73-135, lat 18-54
        // Using a more focused range for campus scenarios
        Arbitrary<Double> longitudes = Arbitraries.doubles().between(116.0, 117.0);
        Arbitrary<Double> latitudes = Arbitraries.doubles().between(39.5, 40.5);
        
        return Combinators.combine(longitudes, latitudes)
                .as(GeoTestPoint::new);
    }

    /**
     * Generator for items at various distances around center
     * Generates items both inside and outside typical search radius
     */
    @Provide
    Arbitrary<List<ItemWithDistance>> itemsAroundCenter() {
        Arbitrary<Double> distances = Arbitraries.doubles().between(10, 15000);
        Arbitrary<Double> bearings = Arbitraries.doubles().between(0, 360);
        
        Arbitrary<ItemWithDistance> itemArbitrary = Combinators.combine(distances, bearings)
                .as(ItemWithDistance::new);
        
        return itemArbitrary.list().ofMinSize(5).ofMaxSize(20);
    }

    /**
     * Generator for items with mixed distances (some inside, some outside radius)
     */
    @Provide
    Arbitrary<List<ItemWithDistance>> mixedDistanceItems() {
        // Items inside radius (10-4000 meters)
        Arbitrary<ItemWithDistance> insideItems = Combinators.combine(
                Arbitraries.doubles().between(10, 4000),
                Arbitraries.doubles().between(0, 360)
        ).as(ItemWithDistance::new);
        
        // Items outside radius (6000-15000 meters)
        Arbitrary<ItemWithDistance> outsideItems = Combinators.combine(
                Arbitraries.doubles().between(6000, 15000),
                Arbitraries.doubles().between(0, 360)
        ).as(ItemWithDistance::new);
        
        // Mix inside and outside items
        return Combinators.combine(
                insideItems.list().ofMinSize(2).ofMaxSize(8),
                outsideItems.list().ofMinSize(2).ofMaxSize(8)
        ).as((inside, outside) -> {
            List<ItemWithDistance> mixed = new ArrayList<>();
            mixed.addAll(inside);
            mixed.addAll(outside);
            return mixed;
        });
    }

    // ==================== Helper Methods ====================

    /**
     * Create an Item entity at a calculated position from center
     */
    private Item createItemAtDistance(GeoTestPoint center, ItemWithDistance iwd, int radius) {
        // Calculate actual position using bearing and distance
        double[] newCoords = calculateDestinationPoint(
                center.longitude, center.latitude, 
                iwd.bearingDegrees, iwd.distanceMeters);
        
        Item item = new Item();
        item.setId((long) (Math.random() * 100000 + 1));
        item.setLongitude(BigDecimal.valueOf(newCoords[0]));
        item.setLatitude(BigDecimal.valueOf(newCoords[1]));
        item.setDeleted(0);
        item.setTitle("Test Item");
        item.setDescription("Test Description");
        item.setType(0);
        item.setStatus(0);
        
        return item;
    }

    /**
     * Create an Item entity at an exact distance from center
     */
    private Item createItemAtExactDistance(GeoTestPoint center, double distanceMeters, Long itemId) {
        // Use a random bearing
        double bearing = Math.random() * 360;
        double[] newCoords = calculateDestinationPoint(
                center.longitude, center.latitude, bearing, distanceMeters);
        
        Item item = new Item();
        item.setId(itemId);
        item.setLongitude(BigDecimal.valueOf(newCoords[0]));
        item.setLatitude(BigDecimal.valueOf(newCoords[1]));
        item.setDeleted(0);
        item.setTitle("Test Item " + itemId);
        item.setDescription("Test Description");
        item.setType(0);
        item.setStatus(0);
        
        return item;
    }

    /**
     * Calculate destination point given start point, bearing and distance
     * Using the Haversine formula inverse
     */
    private double[] calculateDestinationPoint(double lng, double lat, double bearingDeg, double distanceMeters) {
        double bearing = Math.toRadians(bearingDeg);
        double lat1 = Math.toRadians(lat);
        double lng1 = Math.toRadians(lng);
        
        double angularDistance = distanceMeters / EARTH_RADIUS;
        
        double lat2 = Math.asin(
                Math.sin(lat1) * Math.cos(angularDistance) +
                Math.cos(lat1) * Math.sin(angularDistance) * Math.cos(bearing));
        
        double lng2 = lng1 + Math.atan2(
                Math.sin(bearing) * Math.sin(angularDistance) * Math.cos(lat1),
                Math.cos(angularDistance) - Math.sin(lat1) * Math.sin(lat2));
        
        return new double[]{Math.toDegrees(lng2), Math.toDegrees(lat2)};
    }

    /**
     * Calculate Haversine distance between two points
     */
    private double calculateHaversineDistance(double lng1, double lat1, double lng2, double lat2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
}
