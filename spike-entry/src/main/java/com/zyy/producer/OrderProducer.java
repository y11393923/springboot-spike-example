package com.zyy.producer;

import com.alibaba.fastjson.JSONObject;
import com.zyy.constant.SpikeConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
public class OrderProducer implements RabbitTemplate.ConfirmCallback {
    private static final Logger logger = LoggerFactory.getLogger(OrderProducer.class);
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Async
    public void send(JSONObject jsonObject){
        String jsonString = jsonObject.toJSONString();
        String messageId = jsonObject.getString("messageId");
        if (StringUtils.isEmpty(messageId)){
            messageId = UUID.randomUUID().toString().replace("-","");
            jsonObject.put("messageId", messageId);
            jsonString = jsonObject.toJSONString();
        }
        // 封装消息
        Message message = MessageBuilder.withBody(jsonString.getBytes())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding("utf-8")
                .setMessageId(messageId)
                .build();
        // 构建回调返回的数据（消息id）
        this.rabbitTemplate.setMandatory(true);
        this.rabbitTemplate.setConfirmCallback(this);
        CorrelationData correlationData = new CorrelationData(jsonString);
        rabbitTemplate.convertAndSend(SpikeConstant.SPIKE_DIC_QUEUE, message, correlationData);
    }

    /**
     * 生产消息确认机制 生产者往服务器端发送消息的时候，采用应答机制
     * @param correlationData
     * @param ack
     * @param cause
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String id = correlationData.getId();
        logger.info("message id: {}", id);
        if (ack){
            logger.info("message send success");
        }else{
            JSONObject jsonObject = JSONObject.parseObject(id);
            send(jsonObject);
            logger.error("message send fail");
        }
    }
}
