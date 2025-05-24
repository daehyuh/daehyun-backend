package com.example.daehyunbackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StaticController {

    @Value("${admin.password}")
    private String adminPassword;

    @GetMapping("/")
    public String index(@RequestParam String code) {

        if (!code.equals(adminPassword)) {
            return "redirect:https://대현.com";
        }

        return "ad";
    }
}
