package com.zzh.shirokickout.controller;

import com.zzh.shirokickout.entity.User;
import com.zzh.shirokickout.sys.R;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class viewConfig {
    @RequestMapping("/index")
    public String index() {
        return "index";
    }
    @RequestMapping("/unAuth")
    public String unAuth() {
        return "unAuth";
    }
    @RequestMapping("/kickOut")
    public String shiroLogin() {
        return "kickOut";
    }
    @RequestMapping("/login")
    public String login() {
        return "login";
    }
}
