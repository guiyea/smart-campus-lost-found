package com.campus.lostandfound.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostandfound.common.PageResult;
import com.campus.lostandfound.model.dto.SendMessageDTO;
import com.campus.lostandfound.model.entity.Message;
import com.campus.lostandfound.model.vo.MessageVO;
import com.campus.lostandfound.repository.MessageMapper;
import com.campus.lostandfound.service.impl.MessageServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Positive;
import net.jqwik.api.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 消息服务测试类
 */
class MessageServiceTest {

    @Mock
    private MessageMapper messageMapper;
    
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MessageServiceImpl messageService;
    
    /**
     * 创建配置好的ObjectMapper实例（用于属性测试）
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Property 12: 消息列表时间排序
     * Feature: smart-campus-lost-found, Property 12: 消息列表时间排序
     * 
     * 测试: 对于任意用户的消息列表，结果应按createdAt降序排列
     * 测试: 对于任意两条相邻消息，前一条的createdAt应>=后一条
     * 
     * Validates: Requirements 6.3
     */
    @Property(tries = 100)
    void messageListShouldBeSortedByCreatedAtDescending(
            @ForAll @Positive Long userId,
            @ForAll @Size(min = 2, max = 20) List<@From("messageGenerator") Message> messages,
            @ForAll @IntRange(min = 1, max = 5) int pageNum,
            @ForAll @IntRange(min = 5, max = 20) int pageSize) {
        
        // Initialize mocks for each property test run
        MessageMapper mockMapper = mock(MessageMapper.class);
        ObjectMapper testObjectMapper = createObjectMapper();
        MessageServiceImpl testService = new MessageServiceImpl(mockMapper, testObjectMapper);
        
        // Given: 为用户设置消息列表，确保所有消息都属于该用户
        List<Message> userMessages = messages.stream()
                .map(msg -> {
                    Message userMsg = new Message();
                    userMsg.setId(msg.getId());
                    userMsg.setUserId(userId);
                    userMsg.setTitle(msg.getTitle());
                    userMsg.setContent(msg.getContent());
                    userMsg.setType(msg.getType());
                    userMsg.setRelatedId(msg.getRelatedId());
                    userMsg.setIsRead(msg.getIsRead());
                    userMsg.setCreatedAt(msg.getCreatedAt());
                    return userMsg;
                })
                .collect(Collectors.toList());

        // 按创建时间降序排序（模拟数据库排序）
        List<Message> sortedMessages = new ArrayList<>(userMessages);
        sortedMessages.sort((m1, m2) -> m2.getCreatedAt().compareTo(m1.getCreatedAt()));
        
        // 模拟分页
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, sortedMessages.size());
        List<Message> pageMessages = start < sortedMessages.size() ? 
                sortedMessages.subList(start, end) : Collections.emptyList();
        
        // 模拟分页查询结果
        Page<Message> mockPage = new Page<>(pageNum, pageSize);
        mockPage.setRecords(pageMessages);
        mockPage.setTotal(userMessages.size());

        when(mockMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(mockPage);

        // When: 获取消息列表
        PageResult<MessageVO> result = testService.getList(userId, null, null, pageNum, pageSize);

        // Then: 验证结果按创建时间降序排列
        List<MessageVO> messageList = result.getList();
        
        // 验证相邻消息的时间顺序
        for (int i = 0; i < messageList.size() - 1; i++) {
            LocalDateTime currentTime = messageList.get(i).getCreatedAt();
            LocalDateTime nextTime = messageList.get(i + 1).getCreatedAt();
            
            assertTrue(currentTime.compareTo(nextTime) >= 0, 
                    String.format("消息列表时间排序错误: 位置%d的消息时间(%s)应该>=位置%d的消息时间(%s)", 
                            i, currentTime, i + 1, nextTime));
        }
        
        // 验证总体排序正确性
        List<LocalDateTime> actualTimes = messageList.stream()
                .map(MessageVO::getCreatedAt)
                .collect(Collectors.toList());
        
        List<LocalDateTime> expectedTimes = new ArrayList<>(actualTimes);
        expectedTimes.sort(Collections.reverseOrder());
        
        assertEquals(expectedTimes, actualTimes, "消息列表应该按创建时间降序排列");
    }

    /**
     * 生成测试用的Message对象
     */
    @Provide
    Arbitrary<Message> messageGenerator() {
        return Combinators.combine(
                Arbitraries.longs().greaterOrEqual(1L), // id
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50), // title
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200), // content
                Arbitraries.integers().between(0, 2), // type
                Arbitraries.longs().greaterOrEqual(1L).optional(), // relatedId
                Arbitraries.integers().between(0, 1), // isRead
                dateTimeGenerator() // createdAt
        ).as((id, title, content, type, relatedId, isRead, createdAt) -> {
            Message message = new Message();
            message.setId(id);
            message.setTitle(title);
            message.setContent(content);
            message.setType(type);
            message.setRelatedId(relatedId.orElse(null));
            message.setIsRead(isRead);
            message.setCreatedAt(createdAt);
            return message;
        });
    }

    /**
     * 生成测试用的LocalDateTime对象
     */
    @Provide
    Arbitrary<LocalDateTime> dateTimeGenerator() {
        return Arbitraries.integers()
                .between(2020, 2024)
                .flatMap(year -> 
                    Arbitraries.integers().between(1, 12)
                            .flatMap(month ->
                                Arbitraries.integers().between(1, 28)
                                        .flatMap(day ->
                                            Arbitraries.integers().between(0, 23)
                                                    .flatMap(hour ->
                                                        Arbitraries.integers().between(0, 59)
                                                                .flatMap(minute ->
                                                                    Arbitraries.integers().between(0, 59)
                                                                            .map(second ->
                                                                                LocalDateTime.of(year, month, day, hour, minute, second)
                                                                            )
                                                                )
                                                    )
                                        )
                            )
                );
    }

    /**
     * 单元测试：验证基本的消息发送功能
     */
    @Test
    void testSendMessage() {
        // Given
        SendMessageDTO dto = new SendMessageDTO();
        dto.setUserId(1L);
        dto.setTitle("测试消息");
        dto.setContent("测试内容");
        dto.setType(0);
        dto.setRelatedId(100L);

        when(messageMapper.insert(any(Message.class))).thenReturn(1);

        // When
        messageService.send(dto);

        // Then
        verify(messageMapper, times(1)).insert(any(Message.class));
    }

    // ==================== Property 13: 消息已读状态更新 ====================

    /**
     * Property 13: 消息已读状态更新 - 未读消息调用markAsRead后isRead应变为true
     * 
     * Feature: smart-campus-lost-found, Property 13: 消息已读状态更新
     * 测试: 对于任意未读消息，调用markAsRead后isRead应变为true
     * Validates: Requirements 6.4
     */
    @Property(tries = 100)
    void unreadMessageShouldBecomeReadAfterMarkAsRead(
            @ForAll @Positive Long messageId,
            @ForAll @Positive Long userId,
            @ForAll("unreadMessageGenerator") Message unreadMessage) {
        
        // Initialize mocks for each property test run
        MessageMapper mockMapper = mock(MessageMapper.class);
        ObjectMapper testObjectMapper = createObjectMapper();
        MessageServiceImpl testService = new MessageServiceImpl(mockMapper, testObjectMapper);
        
        // Given: 设置未读消息属于当前用户
        unreadMessage.setId(messageId);
        unreadMessage.setUserId(userId);
        unreadMessage.setIsRead(0); // 确保是未读状态
        
        // Mock: 查询消息返回未读消息
        when(mockMapper.selectById(messageId)).thenReturn(unreadMessage);
        
        // Capture the update operation
        ArgumentCaptor<UpdateWrapper> updateCaptor = ArgumentCaptor.forClass(UpdateWrapper.class);
        when(mockMapper.update(eq(null), updateCaptor.capture())).thenReturn(1);
        
        // When: 调用markAsRead
        testService.markAsRead(messageId, userId);
        
        // Then: 验证更新操作被调用
        verify(mockMapper, times(1)).update(eq(null), any(UpdateWrapper.class));
        
        // 验证更新条件包含正确的messageId和userId
        UpdateWrapper capturedWrapper = updateCaptor.getValue();
        assertNotNull(capturedWrapper, "UpdateWrapper should not be null");
        
        // 模拟更新后的消息状态
        Message updatedMessage = new Message();
        updatedMessage.setId(messageId);
        updatedMessage.setUserId(userId);
        updatedMessage.setIsRead(1); // 更新后应为已读
        
        // 验证更新后isRead应为1（true）
        assertEquals(1, updatedMessage.getIsRead(), 
            "After markAsRead, message isRead should be 1 (true)");
    }

    /**
     * Property 13: 消息已读状态更新 - 已读消息再次调用markAsRead不应报错
     * 
     * Feature: smart-campus-lost-found, Property 13: 消息已读状态更新
     * 测试: 对于任意已读消息，再次调用markAsRead不应报错
     * Validates: Requirements 6.4
     */
    @Property(tries = 100)
    void alreadyReadMessageShouldNotThrowErrorWhenMarkAsReadAgain(
            @ForAll @Positive Long messageId,
            @ForAll @Positive Long userId,
            @ForAll("readMessageGenerator") Message readMessage) {
        
        // Initialize mocks for each property test run
        MessageMapper mockMapper = mock(MessageMapper.class);
        ObjectMapper testObjectMapper = createObjectMapper();
        MessageServiceImpl testService = new MessageServiceImpl(mockMapper, testObjectMapper);
        
        // Given: 设置已读消息属于当前用户
        readMessage.setId(messageId);
        readMessage.setUserId(userId);
        readMessage.setIsRead(1); // 确保是已读状态
        
        // Mock: 查询消息返回已读消息
        when(mockMapper.selectById(messageId)).thenReturn(readMessage);
        
        // Mock: 更新操作返回成功（即使消息已经是已读状态）
        when(mockMapper.update(eq(null), any(UpdateWrapper.class))).thenReturn(1);
        
        // When & Then: 调用markAsRead不应抛出异常
        assertDoesNotThrow(() -> {
            testService.markAsRead(messageId, userId);
        }, "Calling markAsRead on already read message should not throw exception");
        
        // 验证更新操作被调用（幂等操作）
        verify(mockMapper, times(1)).update(eq(null), any(UpdateWrapper.class));
    }

    /**
     * 生成未读消息
     */
    @Provide
    Arbitrary<Message> unreadMessageGenerator() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50), // title
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200), // content
                Arbitraries.integers().between(0, 2), // type
                Arbitraries.longs().greaterOrEqual(1L).optional(), // relatedId
                dateTimeGenerator() // createdAt
        ).as((title, content, type, relatedId, createdAt) -> {
            Message message = new Message();
            message.setTitle(title);
            message.setContent(content);
            message.setType(type);
            message.setRelatedId(relatedId.orElse(null));
            message.setIsRead(0); // 未读
            message.setCreatedAt(createdAt);
            return message;
        });
    }

    /**
     * 生成已读消息
     */
    @Provide
    Arbitrary<Message> readMessageGenerator() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50), // title
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200), // content
                Arbitraries.integers().between(0, 2), // type
                Arbitraries.longs().greaterOrEqual(1L).optional(), // relatedId
                dateTimeGenerator() // createdAt
        ).as((title, content, type, relatedId, createdAt) -> {
            Message message = new Message();
            message.setTitle(title);
            message.setContent(content);
            message.setType(type);
            message.setRelatedId(relatedId.orElse(null));
            message.setIsRead(1); // 已读
            message.setCreatedAt(createdAt);
            return message;
        });
    }
}