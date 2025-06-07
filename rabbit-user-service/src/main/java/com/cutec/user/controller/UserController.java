package com.cutec.user.controller;


import com.cutec.common.annotations.NoAuth;
import com.cutec.common.config.UserThreadLocal;
import com.cutec.common.enums.CustomError;
import com.cutec.common.error.CustomizeException;
import com.cutec.common.response.Result;
import com.cutec.meida.api.MediaApi;
import com.cutec.meida.api.vo.Media;
import com.cutec.user.entity.User;
import com.cutec.user.param.LoginParam;
import com.cutec.user.service.UserService;
import com.cutec.user.utils.JWTUtils;
import com.cutec.user.vo.LoginResult;
import com.cutec.user.vo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/")
public class UserController {

    private final UserService userService;
    private final JWTUtils jwtUtils;

    public UserController(UserService userService, JWTUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @NoAuth
    @PostMapping("/login/wechat")
    public Result<String> login(@RequestParam String code) {
        String openId = userService.login(code);
        User byOpenId = userService.findByOpenId(openId);
        if (byOpenId == null) {
            User user = new User();
            user.setOpenId(openId);
            user.setCreateTime(new Date());
            User query = userService.addUser(user);
            String token = jwtUtils.create(query.getUid());
            return new Result<>(token);
        } else {
            String token = jwtUtils.create(byOpenId.getUid());
            return new Result<>(token);
        }
    }

    @NoAuth
    @PostMapping("/auth/login")
    public Result<LoginResult> login(@RequestBody LoginParam loginParam) {
        User user = userService.getByPhone(loginParam.getUsername());
        if (user == null) {
            throw new CustomizeException(CustomError.PASSWORD_ERROR);
        }
        if (user.getPassword().equals(loginParam.getPassword())) {
            String token = jwtUtils.create(user.getUid());
            LoginResult loginResult = new LoginResult();
            loginResult.setAccessToken(token);
            return new Result<>(loginResult);
        } else {
            throw new CustomizeException(CustomError.PASSWORD_ERROR);
        }
    }

    @GetMapping("/user/info")
    public Result<UserInfo> userInfo() {
        Integer userId = UserThreadLocal.user.get();
        User user = userService.getById(userId);
        UserInfo userInfo = new UserInfo();
        userInfo.setRealName(user.getNickname());
        userInfo.setRoles(new ArrayList<>());
        return new Result<>(userInfo);
    }

    @GetMapping("/auth/codes")
    public Result<List<String>> authCodes() {
        return new Result<>(new ArrayList<>());
    }


    @Autowired
    MediaApi mediaApi;

    @GetMapping("/test")
    public Result<Media> test() {
        Result<Media> media = mediaApi.get(4701167);
        return media;
    }
}
