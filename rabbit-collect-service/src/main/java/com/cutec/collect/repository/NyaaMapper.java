package com.cutec.collect.repository;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cutec.collect.entity.Sukebei;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NyaaMapper extends BaseMapper<Sukebei> {
    @Select("SELECT * FROM sukebei WHERE magnet = #{magnet} limit 1")
    List<Sukebei> findByMagnet(String magnet);
}
