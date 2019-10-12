package com.zyy.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.zyy.constant.SpikeConstant;
import com.zyy.entity.SpikeCommodity;
import com.zyy.entity.SpikeOrder;
import com.zyy.enums.ResultCode;
import com.zyy.mapper.SpikeCommodityMapper;
import com.zyy.producer.OrderProducer;
import com.zyy.response.ResponseResult;
import com.zyy.service.SpikeService;
import com.zyy.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SpikeServiceImpl implements SpikeService {
    private static final Logger log = LoggerFactory.getLogger(SpikeServiceImpl.class);

    @Autowired
    private SpikeCommodityMapper spikeCommodityMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private OrderProducer orderProducer;

    /**
     * 每秒生成5个令牌
     */
    private RateLimiter rateLimiter = RateLimiter.create(5d);

    @Override
    public ResponseResult commoditySpike(Long userId, Long commodityId) {
        if (commodityId == null || userId == null){
            return ResponseResult.build(ResultCode.PARAMETER_CANNOT_BE_EMPTY);
        }
        //如果获取到令牌就执行下面逻辑，没获取到重试1秒钟
        if (rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)){
            String spikeUserLimitKey = String.format(SpikeConstant.SPIKE_USER_LIMIT, userId);
            //同一个用户相同时间内不能重复请求
            if (!redisUtil.setIfAbsent(spikeUserLimitKey, String.valueOf(Boolean.TRUE),10)){
                return ResponseResult.build(ResultCode.TOO_MANY_PEOPLE);
            }
            //检测秒杀商品是否存在
            SpikeCommodity spikeCommodity = getSpikeCommodity(commodityId);
            if (null == spikeCommodity){
                return ResponseResult.build(ResultCode.NO_SUCH_ITEM);
            }
            //检查是否在活动时间内
            ResponseResult responseResult = checkActivityTime(spikeCommodity);
            if (null != responseResult){
                log.warn("no longer active time  commodityId: {}", commodityId);
                return responseResult;
            }
            //查询该用户是否秒杀过该商品
            List<Long> userCommodityList = null;
            if(redisUtil.hasKey(SpikeConstant.SPIKE_USER_COMMODITY, String.valueOf(userId))){
                String userCommodityJson = (String) redisUtil.hget(SpikeConstant.SPIKE_USER_COMMODITY, String.valueOf(userId));
                if (!StringUtils.isEmpty(userCommodityJson)){
                    userCommodityList = JSON.parseArray(userCommodityJson, Long.class);
                    if (!CollectionUtils.isEmpty(userCommodityList) && userCommodityList.contains(commodityId)){
                        log.warn("userId: {} repeat spike", userId);
                        return ResponseResult.build(ResultCode.REPEAT_SPIKE);
                    }
                }
            }
            //如果没获取到库存令牌表示已经没有库存
            String stockToken = redisUtil.lpop(String.format(SpikeConstant.SPIKE_COMMODITY_STOCKS_TOKEN, commodityId));
            if (StringUtils.isEmpty(stockToken)){
                log.warn("commodityId: {} no stock available", commodityId);
                return ResponseResult.build(ResultCode.THE_PRODUCT_HAS_BEEN_ROBBED);
            }
            //缓存该用户已经秒杀过的商品
            if (CollectionUtils.isEmpty(userCommodityList)){
                userCommodityList = Lists.newArrayList(commodityId);
            }else{
                userCommodityList.add(commodityId);
            }
            redisUtil.hset(SpikeConstant.SPIKE_USER_COMMODITY, String.valueOf(userId), JSON.toJSONString(userCommodityList));
            //使用mq异步创建订单扣减数据库商品库存
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", userId);
            jsonObject.put("commodityId",commodityId);
            orderProducer.send(jsonObject);
            return ResponseResult.build(ResultCode.BEING_QUEUED);
        }
        return ResponseResult.build(ResultCode.TOO_MANY_PEOPLE);
    }

    @Override
    public ResponseResult spikeResult(Long userId, Long commodityId) {
        if (commodityId == null || userId == null){
            return ResponseResult.build(ResultCode.PARAMETER_CANNOT_BE_EMPTY);
        }
        SpikeCommodity spikeCommodity = getSpikeCommodity(commodityId);
        if (null == spikeCommodity){
            return ResponseResult.build(ResultCode.NO_SUCH_ITEM);
        }
        //如果缓存内存在数据就表示秒杀成功
        String spikeResult = redisUtil.get(String.format(SpikeConstant.SPIKE_SUCCESS_USER_COMMODITY, userId, commodityId));
        if (!StringUtils.isEmpty(spikeResult)){
            SpikeOrder spikeOrder = JSON.parseObject(spikeResult, SpikeOrder.class);
            return ResponseResult.build(spikeOrder);
        }
        //检测活动时间返回友好提示
        ResponseResult responseResult = checkActivityTime(spikeCommodity);
        if (null != responseResult){
            return responseResult;
        }
        return ResponseResult.build(ResultCode.BEING_QUEUED);
    }

    /**
     * 初始化秒杀的商品到缓存
     */
    @PostConstruct
    public void init(){
        List<SpikeCommodity> spikeCommodities = spikeCommodityMapper.findAll();
        if (!CollectionUtils.isEmpty(spikeCommodities)){
            try {
                spikeCommodities.forEach(spikeCommodity -> {
                    //缓存有效期为秒杀开始到结束时间的间隔
                    Long interval = (spikeCommodity.getEndDate().getTime() - spikeCommodity.getStartDate().getTime()) / 1000 + 1;
                    String spikeCommodityKey = String.format(SpikeConstant.SPIKE_COMMODITY, spikeCommodity.getCommodityId());
                    if (!redisUtil.hasKey(spikeCommodityKey)){
                        redisUtil.set(spikeCommodityKey, JSON.toJSONString(spikeCommodity), interval);
                    }
                    String spikeCommodityStockTokenKey = String.format(SpikeConstant.SPIKE_COMMODITY_STOCKS_TOKEN, spikeCommodity.getCommodityId());
                    if (!redisUtil.hasKey(spikeCommodityStockTokenKey)){
                        List<String> stockToken = Lists.newArrayList();
                        for (int i = 0; i < spikeCommodity.getStockCount(); i++) {
                            String token = "spike_" + UUID.randomUUID().toString().replace("-","");
                            stockToken.add(token);
                        }
                        redisUtil.lpush(spikeCommodityStockTokenKey, stockToken);
                        redisUtil.expire(spikeCommodityStockTokenKey, interval);
                    }
                });
            }catch (Exception e){
                log.error("init redis spike commodity error: {}", e);
            }
        }
    }

    private SpikeCommodity getSpikeCommodity(Long commodityId){
        if (commodityId == null){
            return null;
        }
        SpikeCommodity spikeCommodity;
        String commodityJson = redisUtil.get(String.format(SpikeConstant.SPIKE_COMMODITY, commodityId));
        if (StringUtils.isEmpty(commodityJson)){
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("commodity_id", commodityId);
            spikeCommodity = spikeCommodityMapper.selectOne(queryWrapper);
            //缓存有效期为秒杀开始到结束时间的间隔
            Long interval = (spikeCommodity.getEndDate().getTime() - spikeCommodity.getStartDate().getTime()) / 1000 + 1;
            redisUtil.set(String.format(SpikeConstant.SPIKE_COMMODITY, commodityId), JSON.toJSONString(spikeCommodity), interval);
        }else{
            spikeCommodity = JSON.parseObject(commodityJson, SpikeCommodity.class);
        }
        return spikeCommodity;
    }

    private ResponseResult checkActivityTime(SpikeCommodity spikeCommodity){
        //判断是否在活动时间内
        long timeMillis = System.currentTimeMillis();
        if (timeMillis < spikeCommodity.getStartDate().getTime()){
            return ResponseResult.build(ResultCode.SPIKE_HAS_NOT_STARTED);
        }
        if (timeMillis > spikeCommodity.getEndDate().getTime()){
            return ResponseResult.build(ResultCode.SPIKE_IS_OVER);
        }
        return null;
    }

}
