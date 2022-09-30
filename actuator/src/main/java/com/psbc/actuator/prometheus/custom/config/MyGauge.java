package com.psbc.actuator.prometheus.custom.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 第一种方式注册自定义指标
 * inject MeterRegistry
 */
@Component
public class MyGauge {

    private final Map map = new HashMap(16);

    // map_size 1.0
    // map_size_have_tag{app="app1",sys="linux",} 1.0
    public MyGauge(MeterRegistry registry) {
        map.put(1,1);
        registry.gauge("map.size", Tags.empty(), this.map.size()*2);
        // 同名指标可以被覆盖 应该是 2 第二次被覆盖成 1
        registry.gauge("map.size", Tags.empty(), this.map.size());
        registry.gauge("map.size.have.tag", Tags.of("app", "app1", "sys", "linux"), this.map.size());

    }
}


