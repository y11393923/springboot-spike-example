package com.zyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyy.entity.SpikeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SpikeOrderMapper extends BaseMapper<SpikeOrder> {
    @Select("select * from spike_order where user_id = #{userId} and commodity_id = #{commodityId}")
    SpikeOrder findByUserIdAndCommodityId(@Param("userId")Long userId, @Param("commodityId")Long commodityId);
}
