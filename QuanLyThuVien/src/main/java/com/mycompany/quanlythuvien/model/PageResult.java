package com.mycompany.quanlythuvien.model;

import java.util.List;

/**
 * Simple page result for page-indexed pagination (1-based page index)
 */
public class PageResult<T> {
    private List<T> data;
    private int pageIndex;
    private int pageSize;
    private int totalCount;

    public PageResult(List<T> data, int pageIndex, int pageSize, int totalCount) {
        this.data = data;
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
    }

    public List<T> getData() { return data; }
    public int getPageIndex() { return pageIndex; }
    public int getPageSize() { return pageSize; }
    public int getTotalCount() { return totalCount; }

    public int getTotalPages() {
        if (pageSize <= 0) return 0;
        return (totalCount + pageSize - 1) / pageSize;
    }
}