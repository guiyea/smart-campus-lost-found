package com.campus.lostandfound.service.impl;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.http.MethodType;
import com.campus.lostandfound.config.AliyunVisionProperties;
import com.campus.lostandfound.model.entity.Item;
import com.campus.lostandfound.model.entity.ItemTag;
import com.campus.lostandfound.model.vo.RecognitionResult;
import com.campus.lostandfound.model.vo.TagInfo;
import com.campus.lostandfound.repository.ItemMapper;
import com.campus.lostandfound.repository.ItemTagMapper;
import com.campus.lostandfound.service.ImageRecognitionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 图像识别服务实现类
 * 使用阿里云视觉智能API进行图像标签识别
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageRecognitionServiceImpl implements ImageRecognitionService {
    
    private final IAcsClient acsClient;
    private final AliyunVisionProperties visionProperties;
    private final ItemMapper itemMapper;
    private final ItemTagMapper itemTagMapper;
    private final ObjectMapper objectMapper;
    
    /**
     * 系统预定义类别映射
     * 将AI识别的标签映射到系统类别
     */
    private static final Map<String, List<String>> CATEGORY_KEYWORDS = new LinkedHashMap<>();
    
    static {
        // 电子设备
        CATEGORY_KEYWORDS.put("电子设备", Arrays.asList(
                "手机", "电话", "iPhone", "华为", "小米", "OPPO", "vivo", "三星",
                "电脑", "笔记本", "平板", "iPad", "MacBook", "键盘", "鼠标",
                "耳机", "AirPods", "充电器", "数据线", "U盘", "硬盘", "相机",
                "手表", "智能手表", "Apple Watch", "电子", "数码", "电器"
        ));
        
        // 证件卡片
        CATEGORY_KEYWORDS.put("证件卡片", Arrays.asList(
                "身份证", "学生证", "校园卡", "一卡通", "银行卡", "信用卡",
                "驾驶证", "护照", "证件", "卡片", "会员卡", "门禁卡", "工作证"
        ));
        
        // 钥匙
        CATEGORY_KEYWORDS.put("钥匙", Arrays.asList(
                "钥匙", "钥匙扣", "钥匙串", "车钥匙", "门钥匙", "锁"
        ));
        
        // 钱包
        CATEGORY_KEYWORDS.put("钱包", Arrays.asList(
                "钱包", "皮夹", "卡包", "零钱包", "手包", "钱夹"
        ));
        
        // 书籍文具
        CATEGORY_KEYWORDS.put("书籍文具", Arrays.asList(
                "书", "书籍", "教材", "笔记本", "本子", "笔", "铅笔", "钢笔",
                "文具", "尺子", "橡皮", "计算器", "文件", "资料", "作业"
        ));
        
        // 衣物配饰
        CATEGORY_KEYWORDS.put("衣物配饰", Arrays.asList(
                "衣服", "外套", "裤子", "裙子", "帽子", "围巾", "手套",
                "眼镜", "墨镜", "太阳镜", "项链", "手链", "戒指", "耳环",
                "包", "背包", "书包", "手提包", "挎包", "雨伞", "伞"
        ));
        
        // 运动器材
        CATEGORY_KEYWORDS.put("运动器材", Arrays.asList(
                "篮球", "足球", "排球", "羽毛球", "乒乓球", "网球",
                "球拍", "运动", "健身", "跑鞋", "运动鞋", "水杯", "水壶"
        ));
    }
    
    @Override
    public RecognitionResult recognize(String imageUrl) {
        log.info("开始识别图片: {}", imageUrl);
        
        try {
            // 构建请求
            CommonRequest request = new CommonRequest();
            request.setSysMethod(MethodType.POST);
            request.setSysDomain("imagerecog." + visionProperties.getRegionId() + ".aliyuncs.com");
            request.setSysVersion("2019-09-30");
            request.setSysAction("TaggingImage");
            request.putQueryParameter("ImageURL", imageUrl);
            
            // 调用API
            CommonResponse response = acsClient.getCommonResponse(request);
            String responseData = response.getData();
            log.debug("图像识别API响应: {}", responseData);
            
            // 解析响应
            return parseRecognitionResponse(responseData);
            
        } catch (Exception e) {
            log.error("图像识别失败: imageUrl={}", imageUrl, e);
            return RecognitionResult.empty("图像识别服务调用失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<String> extractTags(String imageUrl) {
        RecognitionResult result = recognize(imageUrl);
        if (!result.isSuccess() || result.getTags() == null) {
            return Collections.emptyList();
        }
        return result.getTags().stream()
                .map(TagInfo::getTag)
                .collect(Collectors.toList());
    }
    
    @Override
    public String getCategory(String imageUrl) {
        RecognitionResult result = recognize(imageUrl);
        return result.getCategory() != null ? result.getCategory() : "其他";
    }
    
    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void recognizeAndSaveTags(String imageUrl, Long itemId) {
        log.info("异步识别图片并保存标签: itemId={}, imageUrl={}", itemId, imageUrl);
        
        try {
            // 1. 调用识别API
            RecognitionResult result = recognize(imageUrl);
            
            if (!result.isSuccess()) {
                log.warn("图像识别失败，保留用户手动选择的类别: itemId={}, error={}", 
                        itemId, result.getErrorMessage());
                return;
            }
            
            // 2. 更新物品类别
            if (result.getCategory() != null) {
                Item item = itemMapper.selectById(itemId);
                if (item != null) {
                    // 只有当用户没有手动选择类别时才更新
                    if (item.getCategory() == null || item.getCategory().isEmpty()) {
                        item.setCategory(result.getCategory());
                        itemMapper.updateById(item);
                        log.info("更新物品类别: itemId={}, category={}", itemId, result.getCategory());
                    }
                }
            }
            
            // 3. 保存标签到item_tag表
            if (result.getTags() != null && !result.getTags().isEmpty()) {
                for (TagInfo tagInfo : result.getTags()) {
                    ItemTag itemTag = new ItemTag();
                    itemTag.setItemId(itemId);
                    itemTag.setTag(tagInfo.getTag());
                    itemTag.setConfidence(tagInfo.getConfidence());
                    itemTagMapper.insert(itemTag);
                }
                log.info("保存 {} 个标签: itemId={}", result.getTags().size(), itemId);
            }
            
        } catch (Exception e) {
            log.error("识别并保存标签失败: itemId={}", itemId, e);
            // 降级处理：不抛出异常，保留用户手动选择的类别
        }
    }
    
    /**
     * 解析识别API响应
     */
    private RecognitionResult parseRecognitionResponse(String responseData) {
        try {
            JsonNode root = objectMapper.readTree(responseData);
            
            // 检查是否有错误
            if (root.has("Code") && !"0".equals(root.get("Code").asText())) {
                String errorMsg = root.has("Message") ? root.get("Message").asText() : "未知错误";
                log.warn("图像识别API返回错误: {}", errorMsg);
                return RecognitionResult.empty(errorMsg);
            }
            
            RecognitionResult result = new RecognitionResult();
            result.setSuccess(true);
            result.setRawResponse(responseData);
            
            // 解析标签
            List<TagInfo> tags = new ArrayList<>();
            JsonNode dataNode = root.path("Data");
            JsonNode tagsNode = dataNode.path("Tags");
            
            if (tagsNode.isArray()) {
                for (JsonNode tagNode : tagsNode) {
                    String tagValue = tagNode.path("Value").asText();
                    // API返回的置信度范围是0-100，转换为0-1的小数
                    double confidence = tagNode.path("Confidence").asDouble(0.0) / 100.0;
                    
                    if (tagValue != null && !tagValue.isEmpty()) {
                        tags.add(new TagInfo(tagValue, BigDecimal.valueOf(confidence)));
                    }
                }
            }
            
            result.setTags(tags);
            
            // 根据标签映射到系统类别
            String category = mapToSystemCategory(tags);
            result.setCategory(category);
            
            // 设置类别置信度（使用最高匹配标签的置信度）
            if (!tags.isEmpty()) {
                BigDecimal maxConfidence = tags.stream()
                        .map(TagInfo::getConfidence)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                result.setConfidence(maxConfidence);
            }
            
            log.info("图像识别成功: category={}, tags={}", category, 
                    tags.stream().map(TagInfo::getTag).collect(Collectors.toList()));
            
            return result;
            
        } catch (Exception e) {
            log.error("解析识别响应失败", e);
            return RecognitionResult.empty("解析响应失败: " + e.getMessage());
        }
    }
    
    /**
     * 将识别标签映射到系统预定义类别
     */
    private String mapToSystemCategory(List<TagInfo> tags) {
        if (tags == null || tags.isEmpty()) {
            return "其他";
        }
        
        // 统计每个类别的匹配分数
        Map<String, Double> categoryScores = new HashMap<>();
        
        for (TagInfo tagInfo : tags) {
            String tagLower = tagInfo.getTag().toLowerCase();
            double confidence = tagInfo.getConfidence().doubleValue();
            
            for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
                String category = entry.getKey();
                List<String> keywords = entry.getValue();
                
                for (String keyword : keywords) {
                    if (tagLower.contains(keyword.toLowerCase()) || 
                        keyword.toLowerCase().contains(tagLower)) {
                        double currentScore = categoryScores.getOrDefault(category, 0.0);
                        categoryScores.put(category, currentScore + confidence);
                        break;
                    }
                }
            }
        }
        
        // 返回得分最高的类别
        return categoryScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("其他");
    }
}
