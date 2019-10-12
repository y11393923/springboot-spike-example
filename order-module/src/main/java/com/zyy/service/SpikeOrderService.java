package com.zyy.service;

import com.zyy.entity.SpikeOrder;

public interface SpikeOrderService {
    SpikeOrder findByUserIdAndCommodityId(Long userId, Long commodityId);

    int save(SpikeOrder spikeOrder, Integer version) throws Exception;
}
