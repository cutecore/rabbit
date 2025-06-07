package com.cutec.user.param;

import lombok.Data;

@Data
public class LoginParam {
    private String selectAccount;
    private String username;
    private String password;
    private String captcha;
}
