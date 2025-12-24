package com.campus.lostandfound.model.vo.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 概览数据导出VO - Sheet1
 */
@Data
public class OverviewExportVO {
    
    @ExcelProperty("指标名称")
    @ColumnWidth(20)
    private String metricName;
    
    @ExcelProperty("数值")
    @ColumnWidth(15)
    private String value;
    
    @ExcelProperty("说明")
    @ColumnWidth(30)
    private String description;
    
    public OverviewExportVO() {}
    
    public OverviewExportVO(String metricName, String value, String description) {
        this.metricName = metricName;
        this.value = value;
        this.description = description;
    }
}
