package com.cutec.param;

import lombok.Data;

@Data
public class MediaQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private String field = "number";
    private String sort = "desc";
}
