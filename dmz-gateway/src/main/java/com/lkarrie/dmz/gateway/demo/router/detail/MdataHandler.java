package com.lkarrie.dmz.gateway.demo.router.detail;

import com.lkarrie.dmz.gateway.demo.Constants;
import com.lkarrie.dmz.gateway.demo.router.AbstractRouterHandler;
import com.lkarrie.dmz.gateway.demo.router.RouterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

/**
 * 这里对应路由规则中的 spring.cloud.gateway.routes.id=mdata 所捕获的请求
 */
@Component
public class MdataHandler extends AbstractRouterHandler {

    @Override
    public void handle(String app, String url, String method) {
        // 如果有对应微服务 全部禁止外网发起交互 可以跳过isAllowUrl校验直接throw异常
        if(!this.isAllowUrl(app,url,method)){
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN,Constants.API_FORBIDDEN);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        RouterFactory.register(Constants.MDATA,this);
    }
}
