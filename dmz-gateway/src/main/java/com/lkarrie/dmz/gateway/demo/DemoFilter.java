package com.lkarrie.dmz.gateway.demo;

import com.alibaba.nacos.common.utils.StringUtils;
import com.lkarrie.dmz.gateway.demo.router.AbstractRouterHandler;
import com.lkarrie.dmz.gateway.demo.router.RouterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;

@EnableConfigurationProperties(DemoCustomProperties.class)
@Slf4j
@Component
public class DemoFilter implements GlobalFilter, Ordered {

    private final DemoCustomProperties customProperties;
    private DiscoveryLocatorProperties discoveryLocatorProperties;

    public void setDiscoveryLocatorProperties(DiscoveryLocatorProperties discoveryLocatorProperties) {
        this.discoveryLocatorProperties = discoveryLocatorProperties;
    }

    public DemoFilter(DemoCustomProperties customProperties) {
        this.customProperties = customProperties;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 1;
    }

    /**
     * DMZ 区域拦截器定义
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Route gatewayRoute = exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        ServerHttpRequest request = exchange.getRequest();
        String ip = getIp(request);
        String url = request.getURI().getPath();
        String finalUrl ;
        if(url.startsWith("//")){
            // H5 docker 容器内nginx 反向代理路径处理
            finalUrl = url.replaceFirst("//","/");
            log.info("IP[{}]H5端以[{}]方式访问资源路径[{}]",ip,request.getMethod(),url);
        }else{
            finalUrl = url;
            log.info("IP[{}]PC端以[{}]方式访问资源路径[{}]",ip,request.getMethod(),url);
        }

        /**
         * 公网限制IP 设计思路
         * 获取IP后 进行正则匹配
         * 匹配白名单的IP 直接放行
         * 匹配黑名单的IP 禁止访问
         * 未匹配到的黑白名单的IP 走路由过滤
         */
        if(customProperties.getIpCheckEnabled()){
            Matcher whiteMatcher = customProperties.getWhite().getWhiteIpReg().matcher(ip);
            // 在白名单内 直接放行 不进行后续限制操作
            if(whiteMatcher.find()){
                if (log.isDebugEnabled()) {
                    log.debug("IP[{}]属于白名单直接放行!",ip);
                }
                return chain.filter(exchange);
            }else{
                // 不在白名单内 且不在黑名单 通过IP校验 进行后续限制操作
                Matcher blockMatcher = customProperties.getBlock().getBlockIpReg().matcher(ip);
                if(blockMatcher.find()){
                    if (log.isDebugEnabled()) {
                        log.debug("IP[{}]属于黑名单禁止访问!",ip);
                    }
                    throw new HttpClientErrorException(HttpStatus.FORBIDDEN,Constants.IP_FORBIDDEN.replace("$",ip));
                }
            }
        }

        if(log.isDebugEnabled() && customProperties.getApiAllowEnabled()){
            log.debug("IP[{}]属于公网进行接口过滤!",ip);
        }

        /**
         * 公网限制请求 设计思路
         * 首先将后端Api分组 按 微服务 分为不同route
         * 再对特定微服务的接口进行放行
         * 未匹配到的路由信息 在FinalHandler特殊处理
         */
        if(customProperties.getApiAllowEnabled()){
            Optional<AbstractRouterHandler> optional = Optional.ofNullable(RouterFactory.getInvokeStrategy(gatewayRoute.getId()));
            optional.ifPresentOrElse(u->{
                // 后端request
                u.handle(gatewayRoute.getId(), finalUrl,Objects.requireNonNull(request.getMethod()).toString());
                // 前端资源文件请求
                if (Constants.LAST.equalsIgnoreCase(gatewayRoute.getId())) u.handle(request.getPath());
            },()->{
                log.warn("未创建{}模块Handler!",gatewayRoute.getId());
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN,Constants.API_FORBIDDEN);
            });

            if (log.isDebugEnabled()) {
                log.debug("IP[{}]调用接口[{}]属于放行接口!",ip,url);
            }
            // 后续网关有特殊需要可增加头信息
//            return chain.filter(chainAddHeader(exchange));
            return chain.filter(exchange);
        }

        if(log.isDebugEnabled()){
            log.debug("IP[{}]属于公网但未开启接口校验直接放行!",ip);
        }
        // 没有开启任何限制功能 或者通过IP校验未经过API校验 直接放行
        return chain.filter(exchange);
    }

    /**
     * 获取 IP 地址
     * @param request
     * @return
     */
    private static String getIp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        // HTTP代理或者负载均衡服务器
        String ip = headers.getFirst("X-Forwarded-For");
        // apache http 代理 Proxy-Client-IP 请求头
        if (StringUtils.isBlank(ip) || Constants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = headers.getFirst("Proxy-Client-IP");
        }
        // WL-Proxy-Client-IP webLogic插件 请求头
        if (StringUtils.isBlank(ip) || Constants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = headers.getFirst("WL-Proxy-Client-IP");
        }
        // 客户端IP
        if (StringUtils.isBlank(ip) || Constants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_CLIENT_IP");
        }
        // 代理服务器IP
        if (StringUtils.isBlank(ip) || Constants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
        }
        // nginx代理请求头
        if (StringUtils.isBlank(ip) || Constants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = headers.getFirst("X-Real-IP");
        }
        // Remote Address
        if ((StringUtils.isBlank(ip) || Constants.UNKNOWN.equalsIgnoreCase(ip)) && request.getRemoteAddress() != null) {
            ip = request.getRemoteAddress().getAddress().getHostAddress();
        }
        if (StringUtils.isNotBlank(ip) && ip.contains(",")) {
            // 多次代理后会有多个ip值 第一个ip才是真实ip
            ip = ip.split(",")[0];
        }
        // 本地调试获取到IPV6地址
        if("0:0:0:0:0:0:0:1".equals(ip)){
            ip = "localhost";
        }
        return ip;
    }

    /**
     * 请求头添加信息
     *
     * @param exchange 请求上下文
     * @return exchange
     */
    private ServerWebExchange chainAddHeader(ServerWebExchange exchange) {
        ServerHttpRequest.Builder mutate = exchange.getRequest().mutate();
        mutate.header("From-Dmz", String.valueOf(1));
        return exchange.mutate().request(mutate.build()).build();
    }
}
