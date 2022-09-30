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
@Timed(value = "test.timed" ,description = "测试timed注解")
public class TestTimed {

    /**
     * Timed注解接口 需要请求 返回json结果
     * ResponseEntity
     * @return
     */
//    @Timed(value = "test.timed.v1" ,description = "测试timed注解")
    @GetMapping("/timed/v1")
    public ResponseEntity testTimed1() {
        return ResponseEntity.ok(Boolean.TRUE);
    }

    /**
     * ResponseBody
     * @return
     */
//    @Timed(value = "test.timed.v2" ,description = "测试timed注解")
    @GetMapping("/timed/v2")
    @ResponseBody
    public Boolean testTimed2() {
        return Boolean.TRUE;
    }

}
