package com.lkarrie.dmz.gateway.demo.router.detail;

import com.lkarrie.dmz.gateway.demo.Constants;
import com.lkarrie.dmz.gateway.demo.router.AbstractRouterHandler;
import com.lkarrie.dmz.gateway.demo.router.RouterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

/**
 * 这里对应路由规则中的 spring.cloud.gateway.routes.id=last 所捕获的请求
 * 也就是不已配置微服务名(网关路由id)开头的接口和前端请求
 */
@Component
public class LastHandler extends AbstractRouterHandler {

    /**
     * 处理漏网之鱼
     * @param app
     * @param url
     * @param method
     */
    @Override
    public void handle(String app, String url, String method) {
        //这里可以进行一些自定义的特殊请求拦截操作
//        String someKeyWord = "karrie";
//        if(Arrays.stream(url.split("/")).collect(Collectors.toList()).contains(someKeyWord)){}
    }

    /**
     * 处理前端
     * @param path
     */
    @Override
    public void handle(RequestPath path) {
        List<PathContainer.Element> elements = path.elements();
        // http://localhost:8000/
        if (elements.size() < 2) {
            return;
        }
        if (!isAllowWebFile(path.value())) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN,Constants.WEB_FORBIDDEN);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        RouterFactory.register(Constants.LAST,this);
    }
}
