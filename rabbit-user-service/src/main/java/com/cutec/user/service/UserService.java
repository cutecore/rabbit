package com.cutec.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cutec.user.entity.User;
import com.cutec.user.repository.UserMapper;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    private final UserMapper userMapper;

    @Value("${wechat.appId}")
    private String appId;
    @Value("${wechat.secret}")
    private String secret;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public String login(String code) {
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://api.weixin.qq.com")
//                .build();
//        WechatApi userApi = retrofit.create(WechatApi.class);
//        Call<ResponseBody> responseBodyCall = userApi.loginWithCode(appId, secret, code);
//        Response<ResponseBody> execute = null;
//        try {
//            execute = responseBodyCall.execute();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        String response;
//        try (ResponseBody body = execute.body()) {
//            if (body == null) {
//                throw new RuntimeException();
//            }
//            response = body.string();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        WechatLoginResult result = JsonUtils.toObject(response, WechatLoginResult.class);
//        String openid = result.getOpenid();
//        if (StringUtils.hasText(openid)) {
//            return openid;
//        } else {
//            throw new RuntimeException();
//        }
        return null;
    }

    public @Nullable User getByPhone(String username) {
        return userMapper.getByPhone(username);
    }

    public User findByOpenId(String openId) {
        return userMapper.findByOpenId(openId);
    }

    public User addUser(User user) {
        userMapper.save(user);
        return user;
    }

    public User getById(Integer userId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().ge(User::getUid, userId).last("limit 1");
        return userMapper.selectOne(queryWrapper);
    }
}
