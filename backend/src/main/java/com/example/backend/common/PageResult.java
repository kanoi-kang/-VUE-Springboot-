package com.example.backend.common;

import java.util.List;

public class PageResult<T> {
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;
    private List<T> records;

    public PageResult() {}

    public PageResult(long total, int page, int pageSize, List<T> records) {
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
        this.records = records;
    }

    public static <T> PageResult<T> of(long total, int page, int pageSize, List<T> records) {
        return new PageResult<>(total, page, pageSize, records);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}