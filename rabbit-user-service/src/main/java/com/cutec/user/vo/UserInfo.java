package com.cutec.user.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserInfo {
    private List<String> roles;
    private String realName;
}
