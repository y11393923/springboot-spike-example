package com.zyy.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.rabbitmq.client.Channel;
import com.zyy.constant.SpikeConstant;
import com.zyy.entity.SpikeCommodity;
import com.zyy.entity.SpikeOrder;
import com.zyy.service.SpikeCommodityService;
import com.zyy.service.SpikeOrderService;
import com.zyy.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class OrderConsumer {
    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private SpikeCommodityService spikeCommodityService;
    @Autowired
    private SpikeOrderService spikeOrderService;

    @RabbitListener(queues = SpikeConstant.SPIKE_DIC_QUEUE)
    public void process(Message message, Channel channel){
        try {
            String messageId = message.getMessageProperties().getMessageId();
            String json = new String(message.getBody(), "UTF-8");
            logger.info("spike queue messageId: {}, message: {}", messageId, json);
            JSONObject jsonObject = JSONObject.parseObject(json);
            Long userId = jsonObject.getLong("userId");
            Long commodityId = jsonObject.getLong("commodityId");
            if (userId == null || commodityId == null){
                logger.error("userId or commodity is null  userId: {} commodityId: {}", userId, commodityId);
                return;
            }
            //获取秒杀的商品
            SpikeCommodity spikeCommodity = spikeCommodityService.findByCommodityId(commodityId);
            if (spikeCommodity == null){
                logger.error("commodityId: {} error select no data", commodityId);
                return;
            }
            //判断库存是否充足
            if (spikeCommodity.getStockCount().longValue() <= 0){
                logger.warn("commodityId: {} no stock available stockCount: {}", commodityId, spikeCommodity.getStockCount().longValue());
                return;
            }
            //判断该用户是否秒杀过该商品
            SpikeOrder spikeOrder = spikeOrderService.findByUserIdAndCommodityId(userId, commodityId);
            if (null != spikeOrder){
                logger.error("userId: {} commodityId: {} repeat spike", userId, commodityId);
                String userCommodityJson = (String) redisUtil.hget(SpikeConstant.SPIKE_USER_COMMODITY, String.valueOf(userId));
                List<Long> userCommodityList;
                //如果秒杀过 代表前面的检测漏了数据  这里重新缓存该用户秒杀过的商品数据
                if (!StringUtils.isEmpty(userCommodityJson)){
                    userCommodityList = JSON.parseArray(userCommodityJson, Long.class);
                    if (!CollectionUtils.isEmpty(userCommodityList) && !userCommodityList.contains(commodityId)){
                        userCommodityList.add(commodityId);
                        redisUtil.hset(SpikeConstant.SPIKE_USER_COMMODITY, String.valueOf(userId), JSON.toJSONString(userCommodityList));
                    }
                }else{
                    userCommodityList = Lists.newArrayList(commodityId);
                    redisUtil.hset(SpikeConstant.SPIKE_USER_COMMODITY, String.valueOf(userId), JSON.toJSONString(userCommodityList));
                }
                return;
            }
            //插入订单   这里模拟假数据
            Long orderId = 1L;
            spikeOrder = new SpikeOrder(null, userId, orderId, commodityId);
            int result = spikeOrderService.save(spikeOrder, spikeCommodity.getVersion());
            if (result > 0){
                logger.info("userId: {} commodityId: {}   spike success", userId, commodityId);
                //秒杀成功后把秒杀信息放入缓存，用于判断秒杀结果
                redisUtil.set(String.format(SpikeConstant.SPIKE_SUCCESS_USER_COMMODITY, userId, commodityId), JSON.toJSONString(spikeOrder));
                //更新缓存中秒杀商品的库存  缓存有效期为秒杀开始到结束时间的间隔
                Long interval = (spikeCommodity.getEndDate().getTime() - spikeCommodity.getStartDate().getTime()) / 1000 + 1;
                spikeCommodity.setStockCount(spikeCommodity.getStockCount().longValue() - 1);
                redisUtil.set(String.format(SpikeConstant.SPIKE_COMMODITY, commodityId), JSON.toJSONString(spikeCommodity), interval);
            }
        }catch (Exception e){
            logger.error("spike queue error: {}", e.getMessage());
        }
    }
}
