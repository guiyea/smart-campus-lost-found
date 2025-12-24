package com.campus.lostandfound.model.vo.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDate;

/**
 * 每日明细数据导出VO - Sheet2
 */
@Data
public class DailyStatExportVO {
    
    @ExcelProperty("日期")
    @DateTimeFormat("yyyy-MM-dd")
    @ColumnWidth(15)
    private LocalDate date;
    
    @ExcelProperty("新增用户数")
    @ColumnWidth(12)
    private Integer newUsers;
    
    @ExcelProperty("新增信息数")
    @ColumnWidth(12)
    private Integer newItems;
    
    @ExcelProperty("新增失物信息")
    @ColumnWidth(14)
    private Integer newLostItems;
    
    @ExcelProperty("新增招领信息")
    @ColumnWidth(14)
    private Integer newFoundItems;
    
    @ExcelProperty("匹配成功数")
    @ColumnWidth(12)
    private Integer matchedCount;
    
    public DailyStatExportVO() {}
    
    public DailyStatExportVO(LocalDate date, Integer newUsers, Integer newItems, 
                             Integer newLostItems, Integer newFoundItems, Integer matchedCount) {
        this.date = date;
        this.newUsers = newUsers;
        this.newItems = newItems;
        this.newLostItems = newLostItems;
        this.newFoundItems = newFoundItems;
        this.matchedCount = matchedCount;
    }
}
