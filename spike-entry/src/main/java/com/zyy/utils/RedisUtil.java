package com.zyy.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public String get(String key){
        if (StringUtils.isEmpty(key)){
            return null;
        }
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void set(String key, String value){
        set(key, value, null);
    }

    public void set(String key, String value, Long second){
        if (StringUtils.isEmpty(key)){
            return;
        }
        stringRedisTemplate.opsForValue().set(key, value);
        if (second != null){
            expire(key, second);
        }
    }

    public boolean hasKey(String key){
        if (StringUtils.isEmpty(key)){
            return false;
        }
        return stringRedisTemplate.hasKey(key);
    }

    public boolean hasKey(String key1, String key2){
        if (StringUtils.isEmpty(key1) || StringUtils.isEmpty(key2)){
            return false;
        }
        return stringRedisTemplate.opsForHash().hasKey(key1, key2);
    }

    public Object hget(String key1, String key2){
        if (StringUtils.isEmpty(key1) || StringUtils.isEmpty(key2)){
            return null;
        }
        return stringRedisTemplate.opsForHash().get(key1, key2);
    }

    public void hset(String key1, String key2, Object value){
        if (StringUtils.isEmpty(key1) || StringUtils.isEmpty(key2)){
            return;
        }
        stringRedisTemplate.opsForHash().put(key1, key2, value);
    }

    public Long incr(String key, long num){
        if (StringUtils.isEmpty(key)){
            return null;
        }
        return stringRedisTemplate.boundValueOps(key).increment(num);
    }

    public boolean setIfAbsent(String key, String value){
        if (StringUtils.isEmpty(key)){
            return false;
        }
        return stringRedisTemplate.opsForValue().setIfAbsent(key, value);
    }

    public boolean setIfAbsent(String key, String value, long second){
        boolean flag = setIfAbsent(key, value);
        if(flag){
            expire(key, second);
        }
        return flag;
    }

    public boolean expire(String key, long second){
        if (StringUtils.isEmpty(key)){
            return false;
        }
        return stringRedisTemplate.expire(key, second, TimeUnit.SECONDS);
    }

    public Long lpush(String key, List<String> value){
        if (StringUtils.isEmpty(key)){
            return null;
        }
        return stringRedisTemplate.opsForList().leftPushAll(key, value);
    }


    public String lpop(String key){
        if (StringUtils.isEmpty(key)){
            return null;
        }
        return stringRedisTemplate.opsForList().leftPop(key);
    }

    /**
     * 开启Redis 事务
     */
    public void begin() {
        // 开启Redis 事务权限
        stringRedisTemplate.setEnableTransactionSupport(true);
        // 开启事务
        stringRedisTemplate.multi();
    }

    /**
     * 提交事务
     */
    public void exec() {
        // 成功提交事务
        stringRedisTemplate.exec();
    }

    /**
     * 回滚Redis 事务
     */
    public void discard() {
        stringRedisTemplate.discard();
    }

}
