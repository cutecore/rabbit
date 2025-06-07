package com.cutec.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cutec.entity.Media;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MediaMapper extends BaseMapper<Media> {

    @Select("SELECT * FROM media WHERE number = #{number} FOR UPDATE")
    Media findByNumberForUpdate(Integer number);

    @Select("SELECT * FROM media WHERE number = #{number}")
    Media findByNumber(Integer number);

    @Select("SELECT * FROM media  ORDER BY ${field} ${sort} LIMIT #{limit} OFFSET #{start}")
    List<Media> list(int start, Integer limit, String field, String sort);

    @Select("select * from media where actors is null or actors = '' order by number desc limit 10")
    List<Media> queryMissActors();

    @Select("select * from media where studio is null or studio = '' order by number desc limit 10")
    List<Media> queryMissStudio();

    @Select("select * from media where tag is null or tag = '' order by number desc limit 10")
    List<Media> queryMissTag();
}
