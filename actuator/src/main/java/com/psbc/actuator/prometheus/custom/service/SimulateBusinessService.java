package com.psbc.actuator.prometheus.custom.service;

import com.psbc.actuator.prometheus.custom.config.MyCounter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

@Service
@Slf4j
public class SimulateBusinessService {

    public String redisKey = "core_business_pay";

    // 模拟在redis中记录的数据
    // 注意最值 int 不够
    private Long increaseCount = 0L;

    @Autowired
    // spring boot v2.7.3 版本会有循环依赖
    @Lazy
    private MyCounter myCounter;


    @Async
    public Future<Boolean> pay(){

        // 业务逻辑
        log.info("start...");
        log.info("paying...");
        log.info("end...");
        // 持久化
        // gauge
        increaseGaugeCount(redisKey);
        // counter
        //1111
        myCounter.increment();

        return new AsyncResult<>(true);
    }

    public void increaseGaugeCount(String redisKey) {
        // get date from redis or create
        // 注意 这里 必须做持久化
        // 否侧 重启后 历史指标会清0 下次启动指标从0开始计数
        increaseCount++;
        // 验证 可以减少值
//        if(increaseCount > 3){
//            increaseCount = 0L;
//        }
    }

    public Number getCountFromRedis() {
        return increaseCount;
    }
}
