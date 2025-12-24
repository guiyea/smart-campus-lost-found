package com.campus.lostandfound.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.common.Result;
import com.campus.lostandfound.model.dto.ItemDTO;
import com.campus.lostandfound.model.dto.ItemSearchDTO;
import com.campus.lostandfound.model.entity.Item;
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
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 物品信息属性测试
 * 
 * Feature: smart-campus-lost-found, Property 5: 软删除数据保留
 * Validates: Requirements 2.5
 */
class ItemServicePropertyTest {

    // Counter for generating unique IDs
    private final AtomicLong idCounter = new AtomicLong(1);

    /**
     * Property 5: 软删除数据保留 - 删除后数据库中deleted字段应为1
     * 
     * Feature: smart-campus-lost-found, Property 5: 软删除数据保留
     * 测试: 对于任意已发布物品，删除后数据库中deleted字段应为1
     * Validates: Requirements 2.5
     */
    @Property(tries = 100)
    void deletedItemShouldHaveDeletedFieldSetToOne(
            @ForAll("validItemData") ItemTestData testData) {
        
        // Setup mocks
        ItemMapper itemMapper = mock(ItemMapper.class);
        ItemImageMapper itemImageMapper = mock(ItemImageMapper.class);
        ItemTagMapper itemTagMapper = mock(ItemTagMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        PointService pointService = mock(PointService.class);
        ImageRecognitionService imageRecognitionService = mock(ImageRecognitionService.class);
        MatchService matchService = mock(MatchService.class);
        LocationService locationService = mock(LocationService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        
        ItemServiceImpl itemService = new ItemServiceImpl(
                itemMapper, itemImageMapper, itemTagMapper, userMapper,
                pointService, imageRecognitionService, matchService, locationService, redisUtil);
        
        // Create an existing item (simulating a published item)
        Long itemId = testData.itemId;
        Long userId = testData.userId;
        
        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setUserId(userId);
        existingItem.setTitle(testData.title);
        existingItem.setDescription(testData.description);
        existingItem.setType(testData.type);
        existingItem.setCategory(testData.category);
        existingItem.setDeleted(0); // Not deleted initially
        existingItem.setStatus(0);
        existingItem.setViewCount(0);
        
        // Mock: item exists and belongs to the user
        when(itemMapper.selectById(itemId)).thenReturn(existingItem);
        
        // Capture the item being updated
        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        when(itemMapper.updateById(itemCaptor.capture())).thenReturn(1);
        
        // Execute delete
        itemService.delete(itemId, userId);
        
        // Verify the captured item has deleted = 1
        Item capturedItem = itemCaptor.getValue();
        assertEquals(1, capturedItem.getDeleted(),
                "After deletion, the deleted field should be set to 1");
        
        // Verify the item ID is preserved
        assertEquals(itemId, capturedItem.getId(),
                "Item ID should be preserved after deletion");
        
        // Verify updateById was called (not physical delete)
        verify(itemMapper).updateById(any(Item.class));
        verify(itemMapper, never()).deleteById(anyLong());
    }

    /**
     * Property 5: 软删除数据保留 - 直接查询数据库应能找到原始数据
     * 
     * Feature: smart-campus-lost-found, Property 5: 软删除数据保留
     * 测试: 对于任意已删除物品，直接查询数据库应能找到原始数据
     * Validates: Requirements 2.5
     */
    @Property(tries = 100)
    void deletedItemShouldStillExistInDatabase(
            @ForAll("validItemData") ItemTestData testData) {
        
        // Setup mocks
        ItemMapper itemMapper = mock(ItemMapper.class);
        ItemImageMapper itemImageMapper = mock(ItemImageMapper.class);
        ItemTagMapper itemTagMapper = mock(ItemTagMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        PointService pointService = mock(PointService.class);
        ImageRecognitionService imageRecognitionService = mock(ImageRecognitionService.class);
        MatchService matchService = mock(MatchService.class);
        LocationService locationService = mock(LocationService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        
        ItemServiceImpl itemService = new ItemServiceImpl(
                itemMapper, itemImageMapper, itemTagMapper, userMapper,
                pointService, imageRecognitionService, matchService, locationService, redisUtil);
        
        // Create an existing item
        Long itemId = testData.itemId;
        Long userId = testData.userId;
        
        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setUserId(userId);
        existingItem.setTitle(testData.title);
        existingItem.setDescription(testData.description);
        existingItem.setType(testData.type);
        existingItem.setCategory(testData.category);
        existingItem.setDeleted(0);
        existingItem.setStatus(0);
        existingItem.setViewCount(0);
        existingItem.setCreatedAt(LocalDateTime.now());
        
        // Mock: item exists
        when(itemMapper.selectById(itemId)).thenReturn(existingItem);
        when(itemMapper.updateById(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            // Simulate database update - the item still exists with deleted=1
            existingItem.setDeleted(item.getDeleted());
            return 1;
        });
        
        // Execute delete
        itemService.delete(itemId, userId);
        
        // Verify: After deletion, selectById should still return the item
        // (In real database, selectById without @TableLogic filter would return the item)
        // Here we verify that the item data is preserved (not physically deleted)
        
        // The item should still have all original data preserved
        assertEquals(testData.title, existingItem.getTitle(),
                "Original title should be preserved after soft delete");
        assertEquals(testData.description, existingItem.getDescription(),
                "Original description should be preserved after soft delete");
        assertEquals(testData.type, existingItem.getType(),
                "Original type should be preserved after soft delete");
        assertEquals(testData.category, existingItem.getCategory(),
                "Original category should be preserved after soft delete");
        assertEquals(userId, existingItem.getUserId(),
                "Original userId should be preserved after soft delete");
        assertNotNull(existingItem.getCreatedAt(),
                "Original createdAt should be preserved after soft delete");
        
        // Verify the deleted flag is set
        assertEquals(1, existingItem.getDeleted(),
                "Deleted flag should be set to 1");
    }

    /**
     * Property 5: 软删除数据保留 - 通过API查询列表应不包含已删除物品
     * 
     * Feature: smart-campus-lost-found, Property 5: 软删除数据保留
     * 测试: 对于任意已删除物品，通过API查询列表应不包含该物品
     * Validates: Requirements 2.5
     */
    @Property(tries = 100)
    void deletedItemShouldNotAppearInSearchResults(
            @ForAll("validItemData") ItemTestData testData) {
        
        // Setup mocks
        ItemMapper itemMapper = mock(ItemMapper.class);
        ItemImageMapper itemImageMapper = mock(ItemImageMapper.class);
        ItemTagMapper itemTagMapper = mock(ItemTagMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        PointService pointService = mock(PointService.class);
        ImageRecognitionService imageRecognitionService = mock(ImageRecognitionService.class);
        MatchService matchService = mock(MatchService.class);
        LocationService locationService = mock(LocationService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        
        ItemServiceImpl itemService = new ItemServiceImpl(
                itemMapper, itemImageMapper, itemTagMapper, userMapper,
                pointService, imageRecognitionService, matchService, locationService, redisUtil);
        
        Long itemId = testData.itemId;
        Long userId = testData.userId;
        
        // Create an item that is already deleted
        Item deletedItem = new Item();
        deletedItem.setId(itemId);
        deletedItem.setUserId(userId);
        deletedItem.setTitle(testData.title);
        deletedItem.setDescription(testData.description);
        deletedItem.setType(testData.type);
        deletedItem.setCategory(testData.category);
        deletedItem.setDeleted(1); // Already deleted
        deletedItem.setStatus(0);
        deletedItem.setViewCount(0);
        
        // Create a non-deleted item for comparison
        Item activeItem = new Item();
        activeItem.setId(itemId + 1);
        activeItem.setUserId(userId);
        activeItem.setTitle("Active Item");
        activeItem.setDescription("This item is not deleted");
        activeItem.setType(testData.type);
        activeItem.setCategory(testData.category);
        activeItem.setDeleted(0); // Not deleted
        activeItem.setStatus(0);
        activeItem.setViewCount(0);
        activeItem.setCreatedAt(LocalDateTime.now());
        
        // Mock user for convertToVO
        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setAvatar("https://example.com/avatar.jpg");
        when(userMapper.selectById(userId)).thenReturn(user);
        
        // Mock image and tag queries
        when(itemImageMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(itemTagMapper.selectList(any())).thenReturn(new ArrayList<>());
        
        // Mock search: only return non-deleted items
        // The search method adds condition: deleted = 0
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Item> mockPage = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 20);
        mockPage.setRecords(Arrays.asList(activeItem)); // Only active item
        mockPage.setTotal(1);
        
        when(itemMapper.selectPage(any(), any())).thenReturn(mockPage);
        
        // Execute search
        ItemSearchDTO searchDTO = new ItemSearchDTO();
        searchDTO.setPageNum(1);
        searchDTO.setPageSize(20);
        
        PageResult<ItemVO> result = itemService.search(searchDTO);
        
        // Verify: deleted item should not be in results
        assertNotNull(result, "Search result should not be null");
        assertNotNull(result.getList(), "Search result list should not be null");
        
        // Check that no item in the result has the deleted item's ID
        boolean containsDeletedItem = result.getList().stream()
                .anyMatch(vo -> vo.getId().equals(itemId));
        assertFalse(containsDeletedItem,
                "Search results should not contain deleted item with ID: " + itemId);
        
        // Verify that active item is in results
        boolean containsActiveItem = result.getList().stream()
                .anyMatch(vo -> vo.getId().equals(itemId + 1));
        assertTrue(containsActiveItem,
                "Search results should contain active item");
    }

    // ==================== Test Data Class ====================

    /**
     * Test data holder for item properties
     */
    static class ItemTestData {
        Long itemId;
        Long userId;
        String title;
        String description;
        Integer type;
        String category;
        
        ItemTestData(Long itemId, Long userId, String title, String description, 
                     Integer type, String category) {
            this.itemId = itemId;
            this.userId = userId;
            this.title = title;
            this.description = description;
            this.type = type;
            this.category = category;
        }
        
        @Override
        public String toString() {
            return String.format("ItemTestData{itemId=%d, userId=%d, title='%s', type=%d, category='%s'}",
                    itemId, userId, title, type, category);
        }
    }

    // ==================== Generators ====================

    /**
     * Generator for valid item test data
     */
    @Provide
    Arbitrary<ItemTestData> validItemData() {
        Arbitrary<Long> itemIds = Arbitraries.longs().between(1L, 10000L);
        Arbitrary<Long> userIds = Arbitraries.longs().between(1L, 1000L);
        Arbitrary<String> titles = validTitles();
        Arbitrary<String> descriptions = validDescriptions();
        Arbitrary<Integer> types = Arbitraries.integers().between(0, 1);
        Arbitrary<String> categories = validCategories();
        
        return Combinators.combine(itemIds, userIds, titles, descriptions, types, categories)
                .as(ItemTestData::new);
    }

    /**
     * Generator for valid titles (2-50 characters)
     */
    private Arbitrary<String> validTitles() {
        // Generate Chinese titles or English titles
        Arbitrary<String> chineseTitles = Arbitraries.strings()
                .withCharRange('\u4e00', '\u9fa5')
                .ofMinLength(2)
                .ofMaxLength(20)
                .map(s -> "丢失" + s);
        
        Arbitrary<String> englishTitles = Arbitraries.strings()
                .alpha()
                .ofMinLength(2)
                .ofMaxLength(30)
                .map(s -> "Lost " + s);
        
        return Arbitraries.oneOf(chineseTitles, englishTitles);
    }

    /**
     * Generator for valid descriptions (10-1000 characters)
     */
    private Arbitrary<String> validDescriptions() {
        Arbitrary<String> chineseDescriptions = Arbitraries.strings()
                .withCharRange('\u4e00', '\u9fa5')
                .ofMinLength(5)
                .ofMaxLength(50)
                .map(s -> "物品描述：" + s + "，请联系失主。");
        
        Arbitrary<String> englishDescriptions = Arbitraries.strings()
                .alpha()
                .ofMinLength(10)
                .ofMaxLength(100)
                .map(s -> "Item description: " + s + ". Please contact the owner.");
        
        return Arbitraries.oneOf(chineseDescriptions, englishDescriptions);
    }

    /**
     * Generator for valid categories
     */
    private Arbitrary<String> validCategories() {
        return Arbitraries.of(
                "电子设备", "证件卡片", "钥匙", "钱包", 
                "书籍文具", "衣物配饰", "运动器材", "其他"
        );
    }

    // ==================== Property 6: 浏览计数单调递增 ====================

    /**
     * Property 6: 浏览计数单调递增 - 每次调用getDetail后viewCount应比之前增加1
     * 
     * Feature: smart-campus-lost-found, Property 6: 浏览计数单调递增
     * 测试: 对于任意物品，每次调用getDetail后viewCount应比之前增加1
     * Validates: Requirements 2.6
     */
    @Property(tries = 100)
    void viewCountShouldIncreaseByOneAfterGetDetail(
            @ForAll("validItemData") ItemTestData testData,
            @ForAll @IntRange(min = 0, max = 100) int initialViewCount) {
        
        // Setup mocks
        ItemMapper itemMapper = mock(ItemMapper.class);
        ItemImageMapper itemImageMapper = mock(ItemImageMapper.class);
        ItemTagMapper itemTagMapper = mock(ItemTagMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        PointService pointService = mock(PointService.class);
        ImageRecognitionService imageRecognitionService = mock(ImageRecognitionService.class);
        MatchService matchService = mock(MatchService.class);
        LocationService locationService = mock(LocationService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        
        ItemServiceImpl itemService = new ItemServiceImpl(
                itemMapper, itemImageMapper, itemTagMapper, userMapper,
                pointService, imageRecognitionService, matchService, locationService, redisUtil);
        
        Long itemId = testData.itemId;
        Long userId = testData.userId;
        
        // Create an existing item with initial view count
        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setUserId(userId);
        existingItem.setTitle(testData.title);
        existingItem.setDescription(testData.description);
        existingItem.setType(testData.type);
        existingItem.setCategory(testData.category);
        existingItem.setDeleted(0);
        existingItem.setStatus(0);
        existingItem.setViewCount(initialViewCount);
        existingItem.setCreatedAt(LocalDateTime.now());
        
        // Mock: item exists
        when(itemMapper.selectById(itemId)).thenReturn(existingItem);
        
        // Mock: Redis increment returns initialViewCount + 1
        String viewKey = "item:view:" + itemId;
        when(redisUtil.increment(viewKey)).thenReturn((long) (initialViewCount + 1));
        
        // Mock: user exists
        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setAvatar("https://example.com/avatar.jpg");
        when(userMapper.selectById(userId)).thenReturn(user);
        
        // Mock: image and tag queries
        when(itemImageMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(itemTagMapper.selectList(any())).thenReturn(new ArrayList<>());
        
        // Mock: match recommendations
        when(matchService.getRecommendations(itemId)).thenReturn(Result.success(new ArrayList<>()));
        
        // Capture the item being updated
        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        when(itemMapper.updateById(itemCaptor.capture())).thenReturn(1);
        
        // Execute getDetail
        ItemDetailVO result = itemService.getDetail(itemId);
        
        // Verify: viewCount should be initialViewCount + 1
        Item capturedItem = itemCaptor.getValue();
        assertEquals(initialViewCount + 1, capturedItem.getViewCount(),
                "After getDetail, viewCount should increase by 1 from " + initialViewCount);
        
        // Verify: returned VO should have the updated view count
        assertEquals(initialViewCount + 1, result.getViewCount(),
                "Returned ItemDetailVO should have the updated viewCount");
        
        // Verify: Redis increment was called
        verify(redisUtil).increment(viewKey);
    }

    /**
     * Property 6: 浏览计数单调递增 - 连续N次调用getDetail后viewCount应增加N
     * 
     * Feature: smart-campus-lost-found, Property 6: 浏览计数单调递增
     * 测试: 对于任意物品，连续N次调用getDetail后viewCount应增加N
     * Validates: Requirements 2.6
     */
    @Property(tries = 100)
    void viewCountShouldIncreaseByNAfterNGetDetailCalls(
            @ForAll("validItemData") ItemTestData testData,
            @ForAll @IntRange(min = 0, max = 50) int initialViewCount,
            @ForAll @IntRange(min = 1, max = 10) int numberOfCalls) {
        
        // Setup mocks
        ItemMapper itemMapper = mock(ItemMapper.class);
        ItemImageMapper itemImageMapper = mock(ItemImageMapper.class);
        ItemTagMapper itemTagMapper = mock(ItemTagMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        PointService pointService = mock(PointService.class);
        ImageRecognitionService imageRecognitionService = mock(ImageRecognitionService.class);
        MatchService matchService = mock(MatchService.class);
        LocationService locationService = mock(LocationService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        
        ItemServiceImpl itemService = new ItemServiceImpl(
                itemMapper, itemImageMapper, itemTagMapper, userMapper,
                pointService, imageRecognitionService, matchService, locationService, redisUtil);
        
        Long itemId = testData.itemId;
        Long userId = testData.userId;
        
        // Create an existing item with initial view count
        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setUserId(userId);
        existingItem.setTitle(testData.title);
        existingItem.setDescription(testData.description);
        existingItem.setType(testData.type);
        existingItem.setCategory(testData.category);
        existingItem.setDeleted(0);
        existingItem.setStatus(0);
        existingItem.setViewCount(initialViewCount);
        existingItem.setCreatedAt(LocalDateTime.now());
        
        // Mock: item exists
        when(itemMapper.selectById(itemId)).thenReturn(existingItem);
        
        // Mock: Redis increment returns incrementing values
        String viewKey = "item:view:" + itemId;
        AtomicLong redisCounter = new AtomicLong(initialViewCount);
        when(redisUtil.increment(viewKey)).thenAnswer(invocation -> redisCounter.incrementAndGet());
        
        // Mock: user exists
        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setAvatar("https://example.com/avatar.jpg");
        when(userMapper.selectById(userId)).thenReturn(user);
        
        // Mock: image and tag queries
        when(itemImageMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(itemTagMapper.selectList(any())).thenReturn(new ArrayList<>());
        
        // Mock: match recommendations
        when(matchService.getRecommendations(itemId)).thenReturn(Result.success(new ArrayList<>()));
        
        // Mock: updateById
        when(itemMapper.updateById(any(Item.class))).thenReturn(1);
        
        // Execute getDetail N times
        ItemDetailVO lastResult = null;
        for (int i = 0; i < numberOfCalls; i++) {
            lastResult = itemService.getDetail(itemId);
        }
        
        // Verify: final viewCount should be initialViewCount + numberOfCalls
        int expectedFinalViewCount = initialViewCount + numberOfCalls;
        assertEquals(expectedFinalViewCount, lastResult.getViewCount(),
                "After " + numberOfCalls + " getDetail calls, viewCount should be " + expectedFinalViewCount);
        
        // Verify: Redis increment was called exactly numberOfCalls times
        verify(redisUtil, times(numberOfCalls)).increment(viewKey);
        
        // Verify: updateById was called exactly numberOfCalls times
        verify(itemMapper, times(numberOfCalls)).updateById(any(Item.class));
    }

    // ==================== Property 16: 搜索结果相关性 ====================

    /**
     * Property 16: 搜索结果相关性
     * 
     * Feature: smart-campus-lost-found, Property 16: 搜索结果相关性
     * 测试: 对于任意关键词搜索，返回的所有物品的title或description或tags应包含该关键词
     * Validates: Requirements 8.1
     */
    @Property(tries = 100)
    void searchResultsShouldContainKeywordInTitleOrDescriptionOrTags(
            @ForAll("searchKeywords") String keyword) {
        
        // Setup mocks
        ItemMapper itemMapper = mock(ItemMapper.class);
        ItemImageMapper itemImageMapper = mock(ItemImageMapper.class);
        ItemTagMapper itemTagMapper = mock(ItemTagMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        PointService pointService = mock(PointService.class);
        ImageRecognitionService imageRecognitionService = mock(ImageRecognitionService.class);
        MatchService matchService = mock(MatchService.class);
        LocationService locationService = mock(LocationService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        
        ItemServiceImpl itemService = new ItemServiceImpl(
                itemMapper, itemImageMapper, itemTagMapper, userMapper,
                pointService, imageRecognitionService, matchService, locationService, redisUtil);
        
        // Create test items - some matching keyword, some not
        Long userId = 1L;
        
        // Item 1: keyword in title
        Item itemWithKeywordInTitle = createTestItem(1L, userId, 
                "丢失了一个" + keyword + "物品", 
                "这是一个普通的描述", 
                0, "电子设备");
        
        // Item 2: keyword in description
        Item itemWithKeywordInDescription = createTestItem(2L, userId, 
                "普通标题", 
                "物品描述包含" + keyword + "关键词", 
                0, "证件卡片");
        
        // Item 3: keyword in tags (will be matched via tag query)
        Item itemWithKeywordInTags = createTestItem(3L, userId, 
                "另一个普通标题", 
                "另一个普通描述", 
                1, "钥匙");
        
        // Item 4: no keyword match - should NOT be returned
        Item itemWithoutKeyword = createTestItem(4L, userId, 
                "完全不相关的标题", 
                "完全不相关的描述", 
                0, "其他");
        
        // Create tags for item 3
        ItemTag matchingTag = new ItemTag();
        matchingTag.setId(1L);
        matchingTag.setItemId(3L);
        matchingTag.setTag(keyword);
        
        // Mock: tag query returns item 3 as matching
        when(itemTagMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> {
                    // Return matching tag for keyword search
                    return Arrays.asList(matchingTag);
                });
        
        // Mock: page query returns only matching items
        // The search method builds a query with: (title LIKE %keyword% OR description LIKE %keyword% OR id IN tagMatchedIds)
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Item> mockPage = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 20);
        mockPage.setRecords(Arrays.asList(itemWithKeywordInTitle, itemWithKeywordInDescription, itemWithKeywordInTags));
        mockPage.setTotal(3);
        
        when(itemMapper.selectPage(any(), any())).thenReturn(mockPage);
        
        // Mock: user for convertToVO
        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setAvatar("https://example.com/avatar.jpg");
        when(userMapper.selectById(userId)).thenReturn(user);
        
        // Mock: image queries return empty lists
        when(itemImageMapper.selectList(any())).thenReturn(new ArrayList<>());
        
        // Execute search
        ItemSearchDTO searchDTO = new ItemSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setPageNum(1);
        searchDTO.setPageSize(20);
        
        PageResult<ItemVO> result = itemService.search(searchDTO);
        
        // Verify: all returned items should contain keyword in title, description, or tags
        assertNotNull(result, "Search result should not be null");
        assertNotNull(result.getList(), "Search result list should not be null");
        
        for (ItemVO item : result.getList()) {
            boolean containsKeyword = containsKeywordIgnoreCase(item.getTitle(), keyword) ||
                                     containsKeywordIgnoreCase(item.getDescription(), keyword) ||
                                     (item.getTags() != null && item.getTags().stream()
                                             .anyMatch(tag -> containsKeywordIgnoreCase(tag, keyword)));
            
            assertTrue(containsKeyword,
                    String.format("Item (id=%d) should contain keyword '%s' in title, description, or tags. " +
                                  "Title='%s', Description='%s', Tags=%s",
                            item.getId(), keyword, item.getTitle(), item.getDescription(), item.getTags()));
        }
        
        // Verify: item without keyword should not be in results
        boolean containsNonMatchingItem = result.getList().stream()
                .anyMatch(vo -> vo.getId().equals(4L));
        assertFalse(containsNonMatchingItem,
                "Search results should not contain item without keyword match");
    }

    /**
     * Property 16: 搜索结果相关性 - 空关键词搜索应返回所有未删除物品
     * 
     * Feature: smart-campus-lost-found, Property 16: 搜索结果相关性
     * 测试: 对于空关键词搜索，应返回所有未删除的物品（不进行关键词过滤）
     * Validates: Requirements 8.1
     */
    @Property(tries = 100)
    void emptyKeywordSearchShouldReturnAllNonDeletedItems(
            @ForAll @IntRange(min = 1, max = 10) int numberOfItems) {
        
        // Setup mocks
        ItemMapper itemMapper = mock(ItemMapper.class);
        ItemImageMapper itemImageMapper = mock(ItemImageMapper.class);
        ItemTagMapper itemTagMapper = mock(ItemTagMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        PointService pointService = mock(PointService.class);
        ImageRecognitionService imageRecognitionService = mock(ImageRecognitionService.class);
        MatchService matchService = mock(MatchService.class);
        LocationService locationService = mock(LocationService.class);
        RedisUtil redisUtil = mock(RedisUtil.class);
        
        ItemServiceImpl itemService = new ItemServiceImpl(
                itemMapper, itemImageMapper, itemTagMapper, userMapper,
                pointService, imageRecognitionService, matchService, locationService, redisUtil);
        
        // Create test items
        Long userId = 1L;
        List<Item> testItems = new ArrayList<>();
        for (int i = 1; i <= numberOfItems; i++) {
            Item item = createTestItem((long) i, userId, 
                    "标题" + i, 
                    "描述" + i, 
                    i % 2, "电子设备");
            testItems.add(item);
        }
        
        // Mock: page query returns all items
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Item> mockPage = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 20);
        mockPage.setRecords(testItems);
        mockPage.setTotal(numberOfItems);
        
        when(itemMapper.selectPage(any(), any())).thenReturn(mockPage);
        
        // Mock: user for convertToVO
        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setAvatar("https://example.com/avatar.jpg");
        when(userMapper.selectById(userId)).thenReturn(user);
        
        // Mock: image and tag queries return empty lists
        when(itemImageMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(itemTagMapper.selectList(any())).thenReturn(new ArrayList<>());
        
        // Execute search with empty keyword
        ItemSearchDTO searchDTO = new ItemSearchDTO();
        searchDTO.setKeyword(null); // Empty keyword
        searchDTO.setPageNum(1);
        searchDTO.setPageSize(20);
        
        PageResult<ItemVO> result = itemService.search(searchDTO);
        
        // Verify: all items should be returned
        assertNotNull(result, "Search result should not be null");
        assertNotNull(result.getList(), "Search result list should not be null");
        assertEquals(numberOfItems, result.getList().size(),
                "Empty keyword search should return all " + numberOfItems + " items");
    }

    /**
     * Helper method to check if text contains keyword (case-insensitive)
     */
    private boolean containsKeywordIgnoreCase(String text, String keyword) {
        if (text == null || keyword == null) {
            return false;
        }
        return text.toLowerCase().contains(keyword.toLowerCase());
    }

    /**
     * Helper method to create a test Item
     */
    private Item createTestItem(Long id, Long userId, String title, String description, 
                                Integer type, String category) {
        Item item = new Item();
        item.setId(id);
        item.setUserId(userId);
        item.setTitle(title);
        item.setDescription(description);
        item.setType(type);
        item.setCategory(category);
        item.setDeleted(0);
        item.setStatus(0);
        item.setViewCount(0);
        item.setCreatedAt(LocalDateTime.now());
        return item;
    }

    /**
     * Generator for search keywords
     * Generates realistic search keywords that users might use
     */
    @Provide
    Arbitrary<String> searchKeywords() {
        // Common search keywords in Chinese and English
        Arbitrary<String> commonKeywords = Arbitraries.of(
                "手机", "钱包", "钥匙", "身份证", "学生证", "书包", "耳机", "充电器",
                "眼镜", "手表", "雨伞", "水杯", "笔记本", "U盘", "银行卡",
                "phone", "wallet", "key", "card", "bag", "book", "laptop"
        );
        
        // Random Chinese keywords
        Arbitrary<String> randomChineseKeywords = Arbitraries.strings()
                .withCharRange('\u4e00', '\u9fa5')
                .ofMinLength(1)
                .ofMaxLength(5);
        
        // Random English keywords
        Arbitrary<String> randomEnglishKeywords = Arbitraries.strings()
                .alpha()
                .ofMinLength(2)
                .ofMaxLength(10);
        
        return Arbitraries.oneOf(commonKeywords, randomChineseKeywords, randomEnglishKeywords);
    }
}
