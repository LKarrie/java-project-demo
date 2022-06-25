package com.lkarrie.dmz.gateway.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(DemoCustomProperties.class)
@Slf4j
public class DemoGateWayConfig {

    private final ServerProperties serverProperties;

    public DemoGateWayConfig(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Primary
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes,
                                                             WebProperties webProperties, ObjectProvider<ViewResolver> viewResolvers,
                                                             ServerCodecConfigurer serverCodecConfigurer, ApplicationContext applicationContext) {
        DemoErrorWebExceptionHandler exceptionHandler = new DemoErrorWebExceptionHandler(errorAttributes,
                webProperties.getResources(), this.serverProperties.getError(), applicationContext);
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }

    /**
     * InMemoryRouteDefinitionRepository 路由读取删除新增
     */
//    @Bean
//    public InMemoryRouteDefinitionRepository inMemoryRouteDefinitionRepository() {
//        return new InMemoryRouteDefinitionRepository();
//    }

    /**
     * 配置自定义路由
     *
     * @param builder 路由构建器
     * @return 路由
     */
//    @RefreshScope
//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        RouteLocatorBuilder.Builder route = builder.routes()
//                .route(Constants.ENDPOINTS_ID, r -> r.order(Ordered.HIGHEST_PRECEDENCE + 1000)
//                        .path("/*/actuator/**")
//                        .uri("http://localhost")
//                );
//        return route.build();
//    }

}
