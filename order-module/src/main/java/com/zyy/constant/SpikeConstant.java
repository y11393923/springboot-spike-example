package com.zyy.constant;

public class SpikeConstant {

    /**
     * 用户秒杀过商品key
     */
    public static final String SPIKE_USER_COMMODITY = "spike:user:commodity";

    /**
     * 秒杀商品缓存key
     */
    public static final String SPIKE_COMMODITY = "spike:commodity:%s";


    /**
     * 秒杀成功缓存key
     */
    public static final String SPIKE_SUCCESS_USER_COMMODITY = "spike:success:user:%s:commodity:%s";

    /**
     * 秒杀队列
     */
    public static final String SPIKE_DIC_QUEUE = "spike_queue";

    /**
     * 秒杀交换机
     */
    public static final String SPIKE_EXCHANGE_NAME = "spike_exchange_name";

}
