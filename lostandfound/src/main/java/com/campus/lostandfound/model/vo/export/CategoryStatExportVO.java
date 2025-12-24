package com.campus.lostandfound.model.vo.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 物品分类统计导出VO - Sheet3
 */
@Data
public class CategoryStatExportVO {
    
    @ExcelProperty("物品类别")
    @ColumnWidth(15)
    private String category;
    
    @ExcelProperty("失物数量")
    @ColumnWidth(12)
    private Long lostCount;
    
    @ExcelProperty("招领数量")
    @ColumnWidth(12)
    private Long foundCount;
    
    @ExcelProperty("总数量")
    @ColumnWidth(12)
    private Long totalCount;
    
    @ExcelProperty("匹配成功数")
    @ColumnWidth(12)
    private Long matchedCount;
    
    @ExcelProperty("匹配率(%)")
    @ColumnWidth(12)
    private BigDecimal matchRate;
    
    public CategoryStatExportVO() {}
    
    public CategoryStatExportVO(String category, Long lostCount, Long foundCount, 
                                Long totalCount, Long matchedCount, BigDecimal matchRate) {
        this.category = category;
        this.lostCount = lostCount;
        this.foundCount = foundCount;
        this.totalCount = totalCount;
        this.matchedCount = matchedCount;
        this.matchRate = matchRate;
    }
}
