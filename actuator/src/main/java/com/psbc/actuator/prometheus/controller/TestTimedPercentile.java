package com.psbc.actuator.prometheus.controller;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequestMapping({"/api/test"})
// 客户端侧百分位数计算
// 不可聚合
//@Timed(value = "test.timed" ,description = "测试timed注解",percentiles = {0.5, 0.9, 0.95,0.99})
// 服务段侧百分位数计算
// 通过pm端计算 histogram_quantile(0.9, rate(http_request_duration_seconds_bucket[10m]))
// 目标分位数在本bucket排行 p值/本bucket记录数
// 起始bucket大小+(本bucket宽度)*(目标分位数在本bucket排行/本bucket记录数)
//@Timed(value = "test.timed" ,description = "测试timed注解",histogram = true)
// 当然也可以同时生成相关指标
//test_timed_seconds{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/api/test/timed/v4",quantile="0.99",} 0.0
//test_timed_seconds_bucket{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/api/test/timed/v4",le="0.001",} 0.0
@Timed(value = "test.timed" ,description = "测试timed注解",percentiles = {0.9,0.95,0.99},histogram = true)
public class TestTimedPercentile {

    /**
     * Timed注解接口 需要请求 返回json结果
     * ResponseEntity
     * @return
     */
    @GetMapping("/timed/v4")
    public ResponseEntity testTimed4() {
        return ResponseEntity.ok(Boolean.TRUE);
    }

    /**
     * ResponseBody
     * @return
     */
    @GetMapping("/timed/v5")
    @ResponseBody
    public Boolean testTimed5() {
        return Boolean.TRUE;
    }

}
