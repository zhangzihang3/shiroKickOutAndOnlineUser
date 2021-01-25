package com.zzh.shirokickout.controller;

import com.zzh.shirokickout.entity.User;
import com.zzh.shirokickout.sys.R;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.mgt.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kickOut")
public class kickOutController {
    @Autowired
    private SessionManager sessionManager;
    @PostMapping("/login")
    public R shiroLogin(User user) {
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(user.getUsername(), user.getPassword());
        SecurityUtils.getSubject().login(usernamePasswordToken);
        return R.ok();
    }
    @GetMapping("/loginOut")
    public R shiroLoginOut(User user) {

        SecurityUtils.getSubject().logout();
        return R.ok();
    }
}
