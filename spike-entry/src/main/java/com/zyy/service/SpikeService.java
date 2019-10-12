package com.zyy.service;

import com.zyy.response.ResponseResult;

public interface SpikeService {
    ResponseResult commoditySpike(Long userId, Long commodityId);

    ResponseResult spikeResult(Long userId, Long commodityId);
}
