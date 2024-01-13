package com.harris.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> data;
    private int total;

    private PageResult(int total, List<T> data) {
        this.setData(data);
        this.total = total;
    }

    public static <T> PageResult<T> with(List<T> data, int total) {
        return new PageResult<>(total, data);
    }
}