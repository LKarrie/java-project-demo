package com.psbc.actuator.endpoints;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 实现 InfoContributor 重写contribute方法
 * 生成自定义info指标 指定自定义指标集合的名称和包含指标的map集合即可
 */
@Component
public class TestMyInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("example", Collections.singletonMap("key", "value"));
    }

}
