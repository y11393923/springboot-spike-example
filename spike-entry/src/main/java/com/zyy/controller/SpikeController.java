package com.zyy.controller;

import com.zyy.response.ResponseResult;
import com.zyy.service.SpikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spike")
public class SpikeController {

    @Autowired
    private SpikeService spikeService;

    /**
     * 秒杀入口
     * @param userId   用户Id
     * @param commodityId   商品Id
     * @return
     */
    @GetMapping("/entry/userId/{userId}/commodity/{commodityId}")
    public ResponseResult entry(@PathVariable("userId")Long userId, @PathVariable("commodityId")Long commodityId){
        return spikeService.commoditySpike(userId, commodityId);
    }

    /**
     * 秒杀结果
     * @param userId   用户Id
     * @param commodityId   商品Id
     * @return
     */
    @GetMapping("/result/userId/{userId}/commodity/{commodityId}")
    public ResponseResult result(@PathVariable("userId")Long userId, @PathVariable("commodityId")Long commodityId){
        return spikeService.spikeResult(userId, commodityId);
    }
}
