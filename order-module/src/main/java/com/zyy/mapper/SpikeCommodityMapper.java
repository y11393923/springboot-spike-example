package com.zyy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyy.entity.SpikeCommodity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


@Mapper
public interface SpikeCommodityMapper extends BaseMapper<SpikeCommodity> {
    @Select("select * from spike_commodity where commodity_id = #{commodityId}")
    SpikeCommodity findByCommodityId(@Param("commodityId")Long commodityId);

    @Update("update spike_commodity set stock_count = stock_count + #{stockCount}, version = version + 1 where commodity_id = #{commodityId} and stock_count >0 and version = #{version}")
    int updateStockByCommodityId(@Param("commodityId")Long commodityId, @Param("stockCount")Long stockCount, @Param("version")Integer version);
}
