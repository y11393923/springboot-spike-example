package com.zyy.service.impl;

import com.zyy.entity.SpikeCommodity;
import com.zyy.mapper.SpikeCommodityMapper;
import com.zyy.service.SpikeCommodityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpikeCommodityServiceImpl implements SpikeCommodityService {

    @Autowired
    private SpikeCommodityMapper spikeCommodityMapper;

    @Override
    public SpikeCommodity findByCommodityId(Long commodityId) {
        if (commodityId == null){
            return null;
        }
        return spikeCommodityMapper.findByCommodityId(commodityId);
    }
}
