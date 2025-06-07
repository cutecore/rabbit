package com.cutec.meida.api.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("media")
public class Media implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    //    @Id(keyType = KeyType.Auto)
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer number;

    private String title;
    private String studio;
    private String actors;
    private String tag;
    private String magnet;

    private Boolean download;
    private Boolean deleted;
    private Integer rate;

    private String nyaaFileSize;
    private Integer nyaaSize;
    private String nyaaMagnet;
    private Integer nyaaDownloadNum;
    private Integer seDownloadNum;
    private Boolean fcEndSale;

    private Boolean fromSup;
    private Boolean fromNyaa;
    private Boolean fromFc2db;
    private Boolean fromFc2;
    private Boolean fromSe;
    private Boolean fromPdb;
    private Boolean cen;
    private Integer supViews;
    private Date updateTime;
    private Date createTime;

}