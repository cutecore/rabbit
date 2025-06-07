package com.cutec.collect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@TableName("sukebei")
@Data
public class Sukebei implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private Integer number;
    private String magnet;
    private String fileSize;
    private Integer size;
    private Integer download;
    private String src;

    private Date updateTime;
    private Date createTime;
}