package com.cutec.common.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class PageContent<T> {
    private List<T> items;
    private Long total;
}
