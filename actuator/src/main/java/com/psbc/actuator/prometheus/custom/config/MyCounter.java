package com.psbc.actuator.prometheus.custom.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

/**
 * 第一种方式注册自定义指标
 * inject MeterRegistry
 */
@Component
public class MyCounter {

    Counter counter;

    /**
     * 注册Counter指标
     * @param registry
     */
    public MyCounter(MeterRegistry registry) {
        // Counter 指标名称 和 Tags
        counter = registry.counter("my.counter", Tags.empty());
    }

    /**
     * 提供增长Counter指标的方法 供业务代码调用
     * 调用实例见
     * SimulateBusinessController SimulateBusinessService
     */
    public void increment(){
        // 默认增加1
//        counter.increment();
        // 可设置增加值
        counter.increment(1);
    }

}


