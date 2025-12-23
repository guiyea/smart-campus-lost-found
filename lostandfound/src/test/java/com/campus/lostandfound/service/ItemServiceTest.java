package com.campus.lostandfound.service;

import com.campus.lostandfound.model.dto.ItemDTO;
import com.campus.lostandfound.model.entity.Item;
import com.campus.lostandfound.model.entity.ItemImage;
import com.campus.lostandfound.model.entity.User;
import com.campus.lostandfound.model.vo.ItemVO;
import com.campus.lostandfound.repository.ItemImageMapper;
import com.campus.lostandfound.repository.ItemMapper;
import com.campus.lostandfound.repository.ItemTagMapper;
import com.campus.lostandfound.repository.UserMapper;
import com.campus.lostandfound.service.impl.ItemServiceImpl;
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
}
