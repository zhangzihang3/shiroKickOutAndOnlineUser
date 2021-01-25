package com.zzh.shirokickout.auth.realm;

import com.zzh.shirokickout.entity.User;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class loginRealm extends AuthorizingRealm {
    private Logger log = LoggerFactory.getLogger(loginRealm.class);
    private String KICK_OUT_KEY = "ZZH_KICK_OUT";

    /**
     * @param
     * @return
     * @function 授权
     */

    @Override
    public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        if (session.getAttribute(KICK_OUT_KEY) != null) {
            subject.logout();
            log.info("被踢出的sessionId" + session.getId());
            //此次会话已经被踢出，跳转到踢出页面
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            return simpleAuthorizationInfo;
        }
        return Authorization(principals);
    }

    private SimpleAuthorizationInfo Authorization(PrincipalCollection principals) {
        log.info("授权。。。。。");
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        ArrayList<String> perms = new ArrayList<>();
        Object key = principals.getPrimaryPrincipal();
        User loginUser = new User();
        try {
            BeanUtils.copyProperties(loginUser, key);
        } catch (Exception e) {
        }
        //TODO这里是模拟数据库中的权限表的情况
        if (loginUser.getUsername().equals("shiro")) {
            perms.add("shiro");
            simpleAuthorizationInfo.addRole("shiro");
            simpleAuthorizationInfo.addStringPermissions(perms);
        }
        if (loginUser.getUsername().equals("zzh")) {
            simpleAuthorizationInfo.addRole("index");
            perms.add("index");
            simpleAuthorizationInfo.addStringPermissions(perms);
        }
        log.info("授权完成。。。。。");
        return simpleAuthorizationInfo;
    }



    /**
     * @param
     * @return
     * @function 认证     doGetAuthenticationInfo
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {

        log.info("认证....");
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        // 1.获取用户输入的用户名
        String username = token.getUsername();
        // 2.获取用户输入的密码
        String password = new String(token.getPassword());
        User loginUser = new User();
        loginUser.setUsername(username);
        loginUser.setPassword(password);
        //这里就是把loginUser中的password与参数二中的password作比较（shiro自动完成）
        //参数一，认证的对象
        //参数二，数据库中的密码
        //参数三，就是realm的名称
        SimpleAuthenticationInfo info =
                new SimpleAuthenticationInfo(loginUser, "666", getName());
        log.info("认证完成....");
        return info;
    }

    @Override
    public Class<?> getAuthenticationTokenClass() {
        // 配置该Realm只支持UsernamePasswordToken
        return UsernamePasswordToken.class;
    }
}
