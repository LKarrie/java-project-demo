package com.psbc.actuator.prometheus.custom.factory;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "test")
@Data
// TODO: 2022/9/20
public class MeterProperties {

    private List<Meter> meters;

    @Data
    public static class Meter {
        private String className;
        private String funcName;
        private String tags;
    }
}
