package com.lkarrie.dmz.gateway.demo.router.detail;

import com.lkarrie.dmz.gateway.demo.Constants;
import com.lkarrie.dmz.gateway.demo.router.AbstractRouterHandler;
import com.lkarrie.dmz.gateway.demo.router.RouterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

/**
 * 这里对应路由规则中的 spring.cloud.gateway.routes.id=auth 所捕获的请求
 */
@Component
public class AuthHandler extends AbstractRouterHandler {

    @Override
    public void handle(String app, String url, String method) {
        if(!this.isAllowUrl(app,url,method)){
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN,Constants.API_FORBIDDEN);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        RouterFactory.register(Constants.AUTH,this);
    }
}
