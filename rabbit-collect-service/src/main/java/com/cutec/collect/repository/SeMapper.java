package com.cutec.collect.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cutec.collect.entity.Se;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SeMapper extends BaseMapper<Se> {
    @Select("SELECT * FROM se WHERE magnet = #{magnet}")
    List<Se> findByMagnet(String magnet);


}
