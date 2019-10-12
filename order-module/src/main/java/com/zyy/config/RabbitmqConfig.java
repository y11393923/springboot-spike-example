package com.zyy.config;

import com.zyy.constant.SpikeConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class RabbitmqConfig {


    @Bean
    public Queue spikeDicQueue(){
        return new Queue(SpikeConstant.SPIKE_DIC_QUEUE, true);
    }
    @Bean
    DirectExchange spikeDirectExchange(){
        return new DirectExchange(SpikeConstant.SPIKE_EXCHANGE_NAME);
    }
    @Bean
    Binding bindingExchangeDicQueue(){
        return BindingBuilder.bind(spikeDicQueue()).to(spikeDirectExchange()).with(SpikeConstant.SPIKE_DIC_QUEUE);
    }
}
