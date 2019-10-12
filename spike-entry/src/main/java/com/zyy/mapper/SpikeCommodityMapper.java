package com.zyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyy.entity.SpikeCommodity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SpikeCommodityMapper extends BaseMapper<SpikeCommodity> {
    @Select("select * from spike_commodity")
    List<SpikeCommodity> findAll();
}
