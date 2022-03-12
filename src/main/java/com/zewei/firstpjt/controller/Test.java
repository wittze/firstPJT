package com.zewei.firstpjt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/first")
public class Test {
    @RequestMapping("first")
    @ResponseBody
    public String first() {
        return "test";
    }
}
