package com.campus.lostandfound.service;

import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.model.dto.ItemDTO;
import com.campus.lostandfound.model.entity.Item;
import com.campus.lostandfound.model.entity.ItemImage;
import com.campus.lostandfound.model.entity.ItemTag;
import com.campus.lostandfound.model.entity.User;
import com.campus.lostandfound.model.vo.ItemDetailVO;
import com.campus.lostandfound.model.vo.ItemVO;
import com.campus.lostandfound.model.vo.MatchVO;
import com.campus.lostandfound.repository.ItemImageMapper;
import com.campus.lostandfound.repository.ItemMapper;
import com.campus.lostandfound.repository.ItemTagMapper;
import com.campus.lostandfound.repository.UserMapper;
import com.campus.lostandfound.service.impl.ItemServiceImpl;
import com.campus.lostandfound.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ItemService单元测试
 */
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    
    @Mock
    private ItemMapper itemMapper;
    
    @Mock
    private ItemImageMapper itemImageMapper;
    
    @Mock
    private ItemTagMapper itemTagMapper;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private PointService pointService;
    
    @Mock
    private ImageRecognitionService imageRecognitionService;
    
    @Mock
    private MatchService matchService;
    
    @Mock
    private RedisUtil redisUtil;
    
    @InjectMocks
    private ItemServiceImpl itemService;
    
    private ItemDTO itemDTO;
    private User user;
    
    @BeforeEach
    void setUp() {
        // 准备测试数据
        itemDTO = new ItemDTO();
        itemDTO.setTitle("丢失钱包");
        itemDTO.setDescription("黑色皮质钱包，内有身份证和银行卡");
        itemDTO.setType(0); // 失物
        itemDTO.setCategory("钱包");
        itemDTO.setImages(Arrays.asList(
            "https://example.com/image1.jpg",
            "https://example.com/image2.jpg"
        ));
        itemDTO.setLongitude(new BigDecimal("116.397128"));
        itemDTO.setLatitude(new BigDecimal("39.916527"));
        itemDTO.setLocationDesc("图书馆三楼");
        itemDTO.setEventTime(LocalDateTime.now().minusHours(2));
        
        user = new User();
        user.setId(1L);
        user.setName("张三");
        user.setAvatar("https://example.com/avatar.jpg");
        user.setPoints(100);
    }
    
    @Test
    void testPublishLostItem() {
        // Given
        Long userId = 1L;
        
        // Mock itemMapper.insert to set the ID
        doAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(100L);
            return 1;
        }).when(itemMapper).insert(any(Item.class));
        
        when(userMapper.selectById(userId)).thenReturn(user);
        when(itemImageMapper.selectList(any())).thenReturn(Arrays.asList());
        when(itemTagMapper.selectList(any())).thenReturn(Arrays.asList());
        
        // When
        ItemVO result = itemService.publish(itemDTO, userId);
        
        // Then
        assertNotNull(result);
        assertEquals("丢失钱包", result.getTitle());
        assertEquals("黑色皮质钱包，内有身份证和银行卡", result.getDescription());
        assertEquals(0, result.getType());
        assertEquals("钱包", result.getCategory());
        assertEquals(0, result.getStatus());
        assertEquals(0, result.getViewCount());
        assertEquals("张三", result.getUserName());
        
        // 验证Item被保存
        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemMapper).insert(itemCaptor.capture());
        Item savedItem = itemCaptor.getValue();
        assertEquals(userId, savedItem.getUserId());
        assertEquals(0, savedItem.getStatus());
        assertEquals(0, savedItem.getViewCount());
        assertEquals(0, savedItem.getDeleted());
        
        // 验证图片被保存
        verify(itemImageMapper, times(2)).insert(any(ItemImage.class));
        
        // 验证失物不增加积分
        verify(pointService, never()).addPoints(any(), any(), any(), any());
    }
    
    @Test
    void testPublishFoundItem() {
        // Given
        Long userId = 1L;
        itemDTO.setType(1); // 招领
        
        // Mock itemMapper.insert to set the ID
        doAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(100L);
            return 1;
        }).when(itemMapper).insert(any(Item.class));
        
        when(userMapper.selectById(userId)).thenReturn(user);
        when(itemImageMapper.selectList(any())).thenReturn(Arrays.asList());
        when(itemTagMapper.selectList(any())).thenReturn(Arrays.asList());
        
        // When
        ItemVO result = itemService.publish(itemDTO, userId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getType());
        
        // 验证招领信息增加10积分
        verify(pointService).addPoints(userId, 10, "发布招领信息", 100L);
    }
    
    @Test
    void testPublishWithMultipleImages() {
        // Given
        Long userId = 1L;
        List<String> images = Arrays.asList(
            "https://example.com/img1.jpg",
            "https://example.com/img2.jpg",
            "https://example.com/img3.jpg"
        );
        itemDTO.setImages(images);
        
        // Mock itemMapper.insert to set the ID
        doAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(100L);
            return 1;
        }).when(itemMapper).insert(any(Item.class));
        
        when(userMapper.selectById(userId)).thenReturn(user);
        when(itemImageMapper.selectList(any())).thenReturn(Arrays.asList());
        when(itemTagMapper.selectList(any())).thenReturn(Arrays.asList());
        
        // When
        ItemVO result = itemService.publish(itemDTO, userId);
        
        // Then
        assertNotNull(result);
        
        // 验证3张图片都被保存
        ArgumentCaptor<ItemImage> imageCaptor = ArgumentCaptor.forClass(ItemImage.class);
        verify(itemImageMapper, times(3)).insert(imageCaptor.capture());
        
        List<ItemImage> savedImages = imageCaptor.getAllValues();
        assertEquals(3, savedImages.size());
        assertEquals(0, savedImages.get(0).getSort());
        assertEquals(1, savedImages.get(1).getSort());
        assertEquals(2, savedImages.get(2).getSort());
    }
    
    @Test  
    void testUpdateItem() {
        // Given
        Long itemId = 100L;
        Long userId = 1L;
        
        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setUserId(userId);
        existingItem.setTitle("旧标题");
        existingItem.setDescription("旧描述");
        existingItem.setType(0);
        existingItem.setDeleted(0);
        
        ItemImage oldImage = new ItemImage();
        oldImage.setItemId(itemId);
        oldImage.setUrl("https://example.com/old-image.jpg");
        oldImage.setSort(0);
        
        when(itemMapper.selectById(itemId)).thenReturn(existingItem);
        when(itemImageMapper.selectList(any())).thenReturn(Arrays.asList(oldImage));
        when(userMapper.selectById(userId)).thenReturn(user);
        when(itemTagMapper.selectList(any())).thenReturn(Arrays.asList());
        
        // When
        ItemVO result = itemService.update(itemId, itemDTO, userId);
        
        // Then
        assertNotNull(result);
        assertEquals("丢失钱包", result.getTitle());
        assertEquals("黑色皮质钱包，内有身份证和银行卡", result.getDescription());
        
        // 验证Item被更新
        verify(itemMapper).updateById(any(Item.class));
        
        // 验证旧图片被删除
        verify(itemImageMapper).delete(any());
        
        // 验证新图片被插入
        verify(itemImageMapper, times(2)).insert(any(ItemImage.class));
    }
    
    @Test
    void testUpdateItemNotFound() {
        // Given
        Long itemId = 999L;
        Long userId = 1L;
        
        when(itemMapper.selectById(itemId)).thenReturn(null);
        
        // When & Then
        assertThrows(com.campus.lostandfound.exception.NotFoundException.class, 
            () -> itemService.update(itemId, itemDTO, userId));
    }
    
    @Test
    void testUpdateItemDeleted() {
        // Given
        Long itemId = 100L;
        Long userId = 1L;
        
        Item deletedItem = new Item();
        deletedItem.setId(itemId);
        deletedItem.setUserId(userId);
        deletedItem.setDeleted(1);
        
        when(itemMapper.selectById(itemId)).thenReturn(deletedItem);
        
        // When & Then
        assertThrows(com.campus.lostandfound.exception.NotFoundException.class, 
            () -> itemService.update(itemId, itemDTO, userId));
    }
    
    @Test
    void testUpdateItemForbidden() {
        // Given
        Long itemId = 100L;
        Long userId = 1L;
        Long otherUserId = 2L;
        
        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setUserId(otherUserId); // 不同的用户
        existingItem.setDeleted(0);
        
        when(itemMapper.selectById(itemId)).thenReturn(existingItem);
        
        // When & Then
        assertThrows(com.campus.lostandfound.exception.ForbiddenException.class, 
            () -> itemService.update(itemId, itemDTO, userId));
    }
    
    @Test
    void testUpdateItemWithImageChange() {
        // Given
        Long itemId = 100L;
        Long userId = 1L;
        
        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setUserId(userId);
        existingItem.setDeleted(0);
        
        ItemImage oldImage = new ItemImage();
        oldImage.setItemId(itemId);
        oldImage.setUrl("https://example.com/old-image.jpg");
        oldImage.setSort(0);
        
        // 新图片与旧图片不同
        itemDTO.setImages(Arrays.asList("https://example.com/new-image.jpg"));
        
        when(itemMapper.selectById(itemId)).thenReturn(existingItem);
        when(itemImageMapper.selectList(any())).thenReturn(Arrays.asList(oldImage));
        when(userMapper.selectById(userId)).thenReturn(user);
        when(itemTagMapper.selectList(any())).thenReturn(Arrays.asList());
        
        // When
        ItemVO result = itemService.update(itemId, itemDTO, userId);
        
        // Then
        assertNotNull(result);
        
        // 验证旧标签被删除（因为图片变更）
        verify(itemTagMapper).delete(any());
    }
    
    @Test
    void testUpdateItemWithoutImageChange() {
        // Given
        Long itemId = 100L;
        Long userId = 1L;
        
        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setUserId(userId);
        existingItem.setDeleted(0);
        
        ItemImage oldImage1 = new ItemImage();
        oldImage1.setItemId(itemId);
        oldImage1.setUrl("https://example.com/image1.jpg");
        oldImage1.setSort(0);
        
        ItemImage oldImage2 = new ItemImage();
        oldImage2.setItemId(itemId);
        oldImage2.setUrl("https://example.com/image2.jpg");
        oldImage2.setSort(1);
        
        // 使用相同的图片
        itemDTO.setImages(Arrays.asList(
            "https://example.com/image1.jpg",
            "https://example.com/image2.jpg"
        ));
        
        when(itemMapper.selectById(itemId)).thenReturn(existingItem);
        when(itemImageMapper.selectList(any())).thenReturn(Arrays.asList(oldImage1, oldImage2));
        when(userMapper.selectById(userId)).thenReturn(user);
        when(itemTagMapper.selectList(any())).thenReturn(Arrays.asList());
        
        // When
        ItemVO result = itemService.update(itemId, itemDTO, userId);
        
        // Then
        assertNotNull(result);
        
        // 验证标签没有被删除（因为图片未变更）
        verify(itemTagMapper, never()).delete(any());
    }
    
    @Test
    void testGetDetail() {
        // Given
        Long itemId = 100L;
        
        Item item = new Item();
        item.setId(itemId);
        item.setUserId(1L);
        item.setTitle("丢失钱包");
        item.setDescription("黑色皮质钱包，内有身份证和银行卡");
        item.setType(0);
        item.setCategory("钱包");
        item.setLongitude(new BigDecimal("116.397128"));
        item.setLatitude(new BigDecimal("39.916527"));
        item.setLocationDesc("图书馆三楼");
        item.setEventTime(LocalDateTime.now().minusHours(2));
        item.setStatus(0);
        item.setViewCount(5);
        item.setDeleted(0);
        item.setCreatedAt(LocalDateTime.now().minusHours(3));
        item.setUpdatedAt(LocalDateTime.now().minusHours(1));
        
        ItemImage image1 = new ItemImage();
        image1.setItemId(itemId);
        image1.setUrl("https://example.com/image1.jpg");
        image1.setSort(0);
        
        ItemImage image2 = new ItemImage();
        image2.setItemId(itemId);
        image2.setUrl("https://example.com/image2.jpg");
        image2.setSort(1);
        
        ItemTag tag1 = new ItemTag();
        tag1.setItemId(itemId);
        tag1.setTag("黑色");
        
        ItemTag tag2 = new ItemTag();
        tag2.setItemId(itemId);
        tag2.setTag("皮质");
        
        List<MatchVO> matchRecommendations = Arrays.asList(
            createMockMatchVO(200L, "找到钱包", 1),
            createMockMatchVO(201L, "拾获钱包", 1)
        );
        
        when(itemMapper.selectById(itemId)).thenReturn(item);
        when(redisUtil.increment("item:view:" + itemId)).thenReturn(6L);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(itemImageMapper.selectList(any())).thenReturn(Arrays.asList(image1, image2));
        when(itemTagMapper.selectList(any())).thenReturn(Arrays.asList(tag1, tag2));
        when(matchService.getRecommendations(itemId)).thenReturn(Result.success(matchRecommendations));
        
        // When
        ItemDetailVO result = itemService.getDetail(itemId);
        
        // Then
        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("丢失钱包", result.getTitle());
        assertEquals("黑色皮质钱包，内有身份证和银行卡", result.getDescription());
        assertEquals(0, result.getType());
        assertEquals("钱包", result.getCategory());
        assertEquals("张三", result.getUserName());
        assertEquals("https://example.com/avatar.jpg", result.getUserAvatar());
        assertEquals(6, result.getViewCount()); // 浏览次数应该增加1
        
        // 验证图片列表
        assertNotNull(result.getImages());
        assertEquals(2, result.getImages().size());
        assertEquals("https://example.com/image1.jpg", result.getImages().get(0));
        assertEquals("https://example.com/image2.jpg", result.getImages().get(1));
        
        // 验证标签列表
        assertNotNull(result.getTags());
        assertEquals(2, result.getTags().size());
        assertTrue(result.getTags().contains("黑色"));
        assertTrue(result.getTags().contains("皮质"));
        
        // 验证匹配推荐列表
        assertNotNull(result.getMatchRecommendations());
        assertEquals(2, result.getMatchRecommendations().size());
        assertEquals("找到钱包", result.getMatchRecommendations().get(0).getTitle());
        assertEquals("拾获钱包", result.getMatchRecommendations().get(1).getTitle());
        
        // 验证Redis浏览次数增加
        verify(redisUtil).increment("item:view:" + itemId);
        
        // 验证数据库浏览次数更新
        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemMapper).updateById(itemCaptor.capture());
        Item updatedItem = itemCaptor.getValue();
        assertEquals(6, updatedItem.getViewCount());
        
        // 验证调用了匹配推荐服务
        verify(matchService).getRecommendations(itemId);
    }
    
    @Test
    void testGetDetailItemNotFound() {
        // Given
        Long itemId = 999L;
        
        when(itemMapper.selectById(itemId)).thenReturn(null);
        
        // When & Then
        assertThrows(com.campus.lostandfound.exception.NotFoundException.class, 
            () -> itemService.getDetail(itemId));
    }
    
    @Test
    void testGetDetailItemDeleted() {
        // Given
        Long itemId = 100L;
        
        Item deletedItem = new Item();
        deletedItem.setId(itemId);
        deletedItem.setDeleted(1);
        
        when(itemMapper.selectById(itemId)).thenReturn(deletedItem);
        
        // When & Then
        assertThrows(com.campus.lostandfound.exception.NotFoundException.class, 
            () -> itemService.getDetail(itemId));
    }
    
    private ItemVO createMockItemVO(Long id, String title, Integer type) {
        ItemVO vo = new ItemVO();
        vo.setId(id);
        vo.setTitle(title);
        vo.setType(type);
        vo.setUserId(2L);
        vo.setUserName("李四");
        vo.setDescription("测试描述");
        vo.setCategory("钱包");
        vo.setStatus(0);
        vo.setViewCount(0);
        vo.setCreatedAt(LocalDateTime.now());
        return vo;
    }
    
    private MatchVO createMockMatchVO(Long id, String title, Integer type) {
        MatchVO vo = new MatchVO();
        vo.setId(id);
        vo.setTitle(title);
        vo.setType(type);
        vo.setUserId(2L);
        vo.setUserName("李四");
        vo.setDescription("测试描述");
        vo.setCategory("钱包");
        vo.setStatus(0);
        vo.setViewCount(0);
        vo.setCreatedAt(LocalDateTime.now());
        // 设置匹配分数
        vo.setMatchScore(new java.math.BigDecimal("85.5"));
        vo.setCategoryScore(new java.math.BigDecimal("30"));
        vo.setTagScore(new java.math.BigDecimal("25.5"));
        vo.setTimeScore(new java.math.BigDecimal("15"));
        vo.setLocationScore(new java.math.BigDecimal("15"));
        return vo;
    }
}
