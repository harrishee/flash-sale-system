package com.harris.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PageResult<T> {
    private List<T> data;
    private int total;
    
     // 只能通过 of 静态方法创建对象
    public static <T> PageResult<T> of(List<T> data, int total) {
        return new PageResult<>(data, total);
    }
}
