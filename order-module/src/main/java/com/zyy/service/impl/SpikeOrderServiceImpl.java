package com.zyy.service.impl;

import com.zyy.entity.SpikeOrder;
import com.zyy.mapper.SpikeCommodityMapper;
import com.zyy.mapper.SpikeOrderMapper;
import com.zyy.service.SpikeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpikeOrderServiceImpl implements SpikeOrderService {
    @Autowired
    private SpikeOrderMapper spikeOrderMapper;
    @Autowired
    private SpikeCommodityMapper spikeCommodityMapper;

    @Override
    public SpikeOrder findByUserIdAndCommodityId(Long userId, Long commodityId) {
        if (userId == null || commodityId == null){
            return null;
        }
        return spikeOrderMapper.findByUserIdAndCommodityId(userId, commodityId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(SpikeOrder spikeOrder, Integer version) throws Exception {
        int result = 0;
        if (spikeOrder == null){
            return result;
        }
        result = spikeCommodityMapper.updateStockByCommodityId(spikeOrder.getCommodityId(), -1L , version);
        if (result > 0){
            result = spikeOrderMapper.insert(spikeOrder);
            if (result <= 0){
                result = spikeCommodityMapper.updateStockByCommodityId(spikeOrder.getCommodityId(), 1L , version);
                //如果修改失败就抛异常强制回滚
                if (result <= 0){
                    throw new Exception("update stock error result < 1");
                }
            }
        }
        return result;
    }
}
