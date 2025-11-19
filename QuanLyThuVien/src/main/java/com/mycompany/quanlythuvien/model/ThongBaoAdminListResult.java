package com.mycompany.quanlythuvien.model;

import java.util.List;

/**
 * @author Tien
 */
public class ThongBaoAdminListResult {
    private List<ThongBaoAdmin> items;
    private boolean hasMore;
    private Integer nextCursor;  // ID của thông báo cuối cùng để dùng cho page sau

    public ThongBaoAdminListResult() {
    }

    public ThongBaoAdminListResult(List<ThongBaoAdmin> items, boolean hasMore, Integer nextCursor) {
        this.items = items;
        this.hasMore = hasMore;
        this.nextCursor = nextCursor;
    }

    public List<ThongBaoAdmin> getItems() {
        return items;
    }

    public void setItems(List<ThongBaoAdmin> items) {
        this.items = items;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public Integer getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(Integer nextCursor) {
        this.nextCursor = nextCursor;
    }
}
