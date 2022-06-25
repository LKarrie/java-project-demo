package com.lkarrie.dmz.gateway;

import com.lkarrie.dmz.gateway.demo.DemoCustomProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(DemoCustomProperties.class)
@SpringBootApplication
public class DmzGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(DmzGatewayApplication.class, args);
    }

}
