package com.notificationplatform.dto.response;

import java.util.List;

public class PagedResponse<T> {

    private List<T> data;
    private long total;
    private int limit;
    private int offset;
    private boolean hasMore;

    public PagedResponse() {
    }

    public PagedResponse(List<T> data, long total, int limit, int offset) {
        this.data = data;
        this.total = total;
        this.limit = limit;
        this.offset = offset;
        this.hasMore = (offset + limit) < total;
    }

    // Getters and Setters
    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}

