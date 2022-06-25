package com.lkarrie.dmz.gateway.demo.router;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.shaded.com.google.common.collect.Maps;

import java.util.Map;

/**
 * RouterHandler 工厂
 * handler初始化后会加载进 strategyMap 使用时根据路由id获取对应handler实例 调用不同handle方法过滤对应微服务接口
 */
public class RouterFactory {
    private static final Map<String, AbstractRouterHandler> strategyMap = Maps.newHashMap();

    public static AbstractRouterHandler getInvokeStrategy(String routeName){
        return strategyMap.get(routeName);
    }

    public static void register(String routeName, AbstractRouterHandler routerHandler){
        if(StringUtils.isBlank(routeName) || null == routerHandler){
            return;
        }
        strategyMap.put(routeName, routerHandler);
    }
}
