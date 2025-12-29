package com.campus.lostandfound.integration;

import com.campus.lostandfound.model.dto.LoginDTO;
import com.campus.lostandfound.model.dto.RegisterDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API端点测试
 * 测试所有API端点的请求/响应格式定义
 * 
 * Feature: smart-campus-lost-found
 * 验证所有API端点的数据格式正确性
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("API端点格式测试")
public class ApiEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== 公开端点测试 ====================

    @Test
    @Order(1)
    @DisplayName("公开端点 - 物品列表搜索")
    void testPublicItemSearch() throws Exception {
        mockMvc.perform(get("/api/v1/items")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    @DisplayName("公开端点 - 热门物品")
    void testPublicHotItems() throws Exception {
        mockMvc.perform(get("/api/v1/items/hot"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    @DisplayName("公开端点 - 附近物品")
    void testPublicNearbyItems() throws Exception {
        mockMvc.perform(get("/api/v1/items/nearby")
                .param("lng", "121.4737")
                .param("lat", "31.2304")
                .param("radius", "1000"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    @DisplayName("公开端点 - 积分排行榜")
    void testPublicPointRanking() throws Exception {
        mockMvc.perform(get("/api/v1/points/ranking")
                .param("limit", "10"))
                .andExpect(status().isOk());
    }

    // ==================== 认证端点测试 ====================

    @Test
    @Order(10)
    @DisplayName("认证端点 - 注册参数验证")
    void testRegisterValidation() throws Exception {
        // 测试空参数
        RegisterDTO emptyDTO = new RegisterDTO();
        
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    @DisplayName("认证端点 - 登录参数验证")
    void testLoginValidation() throws Exception {
        // 测试空参数
        LoginDTO emptyDTO = new LoginDTO();
        
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== 受保护端点测试（无认证） ====================

    @Test
    @Order(20)
    @DisplayName("受保护端点 - 无认证访问用户信息")
    void testProtectedUserInfoWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(21)
    @DisplayName("受保护端点 - 无认证发布物品")
    void testProtectedPublishItemWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/v1/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(22)
    @DisplayName("受保护端点 - 无认证访问消息列表")
    void testProtectedMessagesWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/messages"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(23)
    @DisplayName("受保护端点 - 无认证访问积分明细")
    void testProtectedPointsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/points"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(24)
    @DisplayName("受保护端点 - 无认证访问管理后台")
    void testProtectedAdminWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/admin/statistics"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 无效令牌测试 ====================

    @Test
    @Order(30)
    @DisplayName("无效令牌 - 访问用户信息")
    void testInvalidTokenUserInfo() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(31)
    @DisplayName("无效令牌 - 发布物品")
    void testInvalidTokenPublishItem() throws Exception {
        mockMvc.perform(post("/api/v1/items")
                .header("Authorization", "Bearer invalid_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 请求格式测试 ====================

    @Test
    @Order(40)
    @DisplayName("请求格式 - 无效JSON")
    void testInvalidJsonFormat() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(41)
    @DisplayName("请求格式 - 缺少Content-Type")
    void testMissingContentType() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .content("{}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ==================== 分页参数测试 ====================

    @Test
    @Order(50)
    @DisplayName("分页参数 - 默认分页")
    void testDefaultPagination() throws Exception {
        mockMvc.perform(get("/api/v1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(51)
    @DisplayName("分页参数 - 自定义分页")
    void testCustomPagination() throws Exception {
        mockMvc.perform(get("/api/v1/items")
                .param("pageNum", "2")
                .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== 搜索筛选测试 ====================

    @Test
    @Order(60)
    @DisplayName("搜索筛选 - 关键词搜索")
    void testKeywordSearch() throws Exception {
        mockMvc.perform(get("/api/v1/items")
                .param("keyword", "手机"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(61)
    @DisplayName("搜索筛选 - 类型筛选")
    void testTypeFilter() throws Exception {
        mockMvc.perform(get("/api/v1/items")
                .param("type", "0"))  // 失物
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(62)
    @DisplayName("搜索筛选 - 类别筛选")
    void testCategoryFilter() throws Exception {
        mockMvc.perform(get("/api/v1/items")
                .param("category", "电子设备"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(63)
    @DisplayName("搜索筛选 - 组合筛选")
    void testCombinedFilter() throws Exception {
        mockMvc.perform(get("/api/v1/items")
                .param("keyword", "手机")
                .param("type", "1")
                .param("category", "电子设备")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== 匹配推荐测试 ====================

    @Test
    @Order(70)
    @DisplayName("匹配推荐 - 获取物品推荐（不存在的物品）")
    void testMatchRecommendationsForNonExistentItem() throws Exception {
        mockMvc.perform(get("/api/v1/matches/recommendations/999999"))
                .andExpect(status().isOk());
    }

    // ==================== 健康检查测试 ====================

    @Test
    @Order(80)
    @DisplayName("健康检查 - Actuator健康端点")
    void testActuatorHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
