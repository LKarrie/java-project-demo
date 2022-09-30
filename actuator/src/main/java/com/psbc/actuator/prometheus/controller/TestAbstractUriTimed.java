package com.psbc.actuator.prometheus.controller;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping({"/api/test/abstract"})
@Timed(value = "test.abstract.uri.timed" ,description = "测试timed注解",percentiles = {0.9,0.95,0.99})
public class TestAbstractUriTimed {

    /**
     * Timed注解接口 需要请求 返回json结果
     * ResponseEntity
     * @return
     */
//    @Timed(value = "test.timed.v1" ,description = "测试timed注解")
    @GetMapping("/timed/{path}")
    public ResponseEntity testAbstractTimed(@PathVariable String path) {
        System.out.println(path);
        return ResponseEntity.ok(Boolean.TRUE);
    }

}
