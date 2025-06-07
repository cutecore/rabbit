package com.cutec.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@TableName("user")
public class User implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer uid;

    private String nickname;

    private String email;

    private String idCard;

    private String phone;

    private Date createTime;

    private Date updateTime;

    private String cityCode;

    private String proName;

    private Date birth;

    private Integer age;

    private String password;

    private Boolean ban;

    private String openId;

}
