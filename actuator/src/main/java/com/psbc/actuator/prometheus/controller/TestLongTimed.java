package com.psbc.actuator.prometheus.controller;

import io.micrometer.core.annotation.Timed;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequestMapping({"/api/test"})
public class TestLongTimed {

    /**
     * longTask 有BUG 建议不要使用
     * 修复方案
     * 见覆盖的源码类 WebMvcMetricsFilter 144 行
     */

    @SneakyThrows
    @Timed(value = "test.long.timed" ,description = "测试long_timed注解", longTask = true)
    @GetMapping("/timed/v3")
    @ResponseBody
    public Boolean testTimed3(@RequestParam Integer second) {
        new Thread().sleep(second*1000);
        return Boolean.TRUE;
    }

    @Timed(value = "test.timed" ,description = "测试timed注解")
    @GetMapping("/timed/v6")
    @ResponseBody
    public Boolean testTimed6() {
        return Boolean.TRUE;
    }

}
