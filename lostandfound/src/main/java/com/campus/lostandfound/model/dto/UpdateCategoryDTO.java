package com.campus.lostandfound.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新物品类别DTO
 */
@Data
public class UpdateCategoryDTO {
    
    /**
     * 物品类别
     * 预定义类别: 电子设备、证件卡片、钥匙、钱包、书籍文具、衣物配饰、运动器材、其他
     */
    @NotBlank(message = "类别不能为空")
    @Size(max = 20, message = "类别长度不能超过20个字符")
    private String category;
}
