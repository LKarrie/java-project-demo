package com.psbc.actuator.prometheus.custom.controller;

import com.psbc.actuator.prometheus.custom.service.SimulateBusinessService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequestMapping({"/api/simulate/business"})
public class SimulateBusinessController {

    final SimulateBusinessService simulateBusinessService;

    public SimulateBusinessController(SimulateBusinessService simulateBusinessService) {
        this.simulateBusinessService = simulateBusinessService;
    }

    @SneakyThrows
    @ResponseBody
    @GetMapping("/pay")
    public ResponseEntity pay() {
        Boolean res = simulateBusinessService.pay().get();
        return ResponseEntity.ok(res);
    }

}
