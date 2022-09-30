package com.psbc.actuator.prometheus.custom.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.psbc.actuator.prometheus.custom.service.SimulateBusinessService;

/**
 * 第二种方式注册自定义指标
 * 推荐 使用这种方式
 * MeterBinder
 */
@Configuration
@Slf4j
public class MyMeterBinderConfiguration {

    /**
     * 公共标签可以 从配置获取 例如所属应用 系统 区域等等
     * 代码注入tag 保证顺序
     */
    @Value("${spring.application.name}")
    private String application;

    private final SimulateBusinessService simulateBusinessService;


    public MyMeterBinderConfiguration(SimulateBusinessService simulateBusinessService) {
        this.simulateBusinessService = simulateBusinessService;
    }

    /**
     * 默认情况下，所有MeterBinder bean中的指标都会自动绑定到Spring管理的MeterRegistry
     *
     * @param simulateBusinessService
     * @return
     */
    @Bean
    public MeterBinder payGaugeCount(SimulateBusinessService simulateBusinessService) {
        // 绑定获取指标函数 并指定Tags
        return (registry) -> Gauge.builder("core.pay.gauge",simulateBusinessService::getCountFromRedis).tags("sys","core","app",application).register(registry);
    }

    //@Bean
    //Other MeterBinder
}
