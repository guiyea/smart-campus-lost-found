package com.campus.lostandfound.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 分页响应结果类
 * @param <T> 列表数据类型
 */
@Schema(description = "分页响应结果")
public class PageResult<T> {
    @Schema(description = "数据列表")
    private List<T> list;
    @Schema(description = "总记录数", example = "100")
    private Long total;
    @Schema(description = "当前页码", example = "1")
    private Integer pageNum;
    @Schema(description = "每页大小", example = "20")
    private Integer pageSize;

    public PageResult() {
    }

    public PageResult(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    // Getters and Setters
    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
