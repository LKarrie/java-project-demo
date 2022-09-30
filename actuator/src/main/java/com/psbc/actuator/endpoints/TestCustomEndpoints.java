package com.psbc.actuator.endpoints;

import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.stereotype.Component;

/**
 * @WebEndpoint 注解指定 Endpoint名称
 * @ReadOperation 提供读接口
 * @WriteOperation 提供写接口
 */
@WebEndpoint(id= "testWebEndpoint")
@Component
public class TestCustomEndpoints {

    // init
    private CustomData customData = new CustomData("", 1);


    /**
     * get customData value
     * http://localhost:9081/actuator/testWebEndpoint
     *
     * @return
     */
    @ReadOperation
    public CustomData getData() {
        return customData;
    }

    /**
     * post to change customData
     * http://localhost:9081/actuator/testWebEndpoint
     * request body
     * {
     *     "name": "test",
     *     "counter": 42
     * }
     *
     * @param name
     * @param counter
     */
    @WriteOperation
    public Boolean updateData(String name, int counter) {
        // injects "test" and 42
        customData.setTestString(name);
        customData.setTestInteger(counter);
        return Boolean.TRUE;
    }
}
