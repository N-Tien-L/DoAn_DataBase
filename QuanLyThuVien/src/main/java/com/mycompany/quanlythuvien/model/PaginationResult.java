package com.mycompany.quanlythuvien.model;

import java.util.List;

/**
 * Kết quả phân trang - Cursor-based pagination
 * 
 * @author Tien
 */
public class PaginationResult<T> {
    private List<T> data; // Danh sách dữ liệu
    private int nextCursor; // Con trỏ để lấy trang tiếp theo (-1 nếu không có trang tiếp theo)
    private int previousCursor; // Con trỏ để lấy trang trước đó (-1 nếu không có trang trước)
    private int totalCount; // Tổng số bản ghi
    private boolean hasNext; // Có trang tiếp theo?
    private boolean hasPrevious; // Có trang trước?
    private int pageSize; // Kích thước trang
    private int currentCursor; // Con trỏ hiện tại

    public PaginationResult(List<T> data, int nextCursor, int previousCursor,
            int totalCount, int pageSize, int currentCursor) {
        this.data = data;
        this.nextCursor = nextCursor;
        this.previousCursor = previousCursor;
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.currentCursor = currentCursor;
        this.hasNext = nextCursor != -1;
        this.hasPrevious = previousCursor != -1;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(int nextCursor) {
        this.nextCursor = nextCursor;
        this.hasNext = nextCursor != -1;
    }

    public int getPreviousCursor() {
        return previousCursor;
    }

    public void setPreviousCursor(int previousCursor) {
        this.previousCursor = previousCursor;
        this.hasPrevious = previousCursor != -1;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentCursor() {
        return currentCursor;
    }

    public void setCurrentCursor(int currentCursor) {
        this.currentCursor = currentCursor;
    }
}
