package com.example.demo.TEST_001.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        log.info("index 메서드 call");
        System.out.println("Index 메서드 call");
        return "index";
    }
}
