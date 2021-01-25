package com.zzh.shirokickout.listeners;

import com.zzh.shirokickout.entity.User;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListenerAdapter;

import javax.annotation.Resource;

public class ShiroSessionListener extends SessionListenerAdapter {
    // session创建
    @Override
    public void onStart(Session session) {
        super.onStart(session);
        System.out.println("session创建，sessionId：" + session.getId());
    }

    // session停止
    @Override
    public void onStop(Session session) {
        //TODO 修改数据库当前用户的登录状态字段
        //登录成功后设置过这个Attribute的
        User loginUser = (User) session.getAttribute("loginUserName");
        System.out.println("session停止，sessionId:" + session.getId() + "，用户：" + loginUser);
    }

    // session失效
    @Override
    public void onExpiration(Session session) {
        //TODO 修改数据库当前用户的登录状态字段
        //登录成功后设置过这个Attribute的
        User loginUser = (User) session.getAttribute("loginUserName");
        System.out.println("session失效，sessionId:" + session.getId() + "，用户：" + loginUser);
    }

}