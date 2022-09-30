package com.psbc.actuator.endpoints;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * 实现 HealthIndicator 重新health方法 增加自定义的检查健康的方法即可
 * 生成健康检查名为 HealthIndicator前的字符串 例如TestMyHealthIndicator 生成健康检查名称为 testMy
 */
@Component
class TestMyHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        int errorCode = check();
        if (errorCode != 0) {
            return Health.down().withDetail("Error Code", errorCode).build();
        }
        return Health.up().build();
    }

    private int check() {
        // perform some specific health check
        return 0;
    }

}
