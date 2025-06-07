package com.cutec.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cutec.user.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from user where open_id = #{openId}")
    User findByOpenId(String openId);

    @Select("select * from user where phone = #{username}")
    User getByPhone(String username);

    @Insert("insert into user (open_id, phone, nick_name)" +
            " values (#{openId}, #{phone}, #{nickName})")
    void save(User user);
}
