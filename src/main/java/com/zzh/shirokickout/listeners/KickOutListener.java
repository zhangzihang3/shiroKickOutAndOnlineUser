package com.zzh.shirokickout.listeners;

import com.zzh.shirokickout.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * @author 张子行
 * @class 踢出监听器
 */
@Slf4j
public class KickOutListener implements AuthenticationListener {
    private String KICK_OUT_KEY = "ZZH_KICK_OUT";
    private String REDIS_LIST_KEY = null;
    //最大人数限制
    private Integer MAX_LOGIN_NUM = 1;
    private Boolean enbleKickOut = true;
    //默认当前用户登录了，是把上一个用户给踢掉
    private Boolean kickOutOrder = true;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SessionManager sessionManager;

    /**
     * @param
     * @method shiro登录成功后调用
     */
    @Override
    public void onSuccess(AuthenticationToken token, AuthenticationInfo info) {
        //登录成功后的subject
        Subject subject = SecurityUtils.getSubject();
        ListOperations redisList = redisTemplate.opsForList();
        Session session = subject.getSession();
        //为了实现在线用户
        session.setAttribute("loginUserName", info.getPrincipals().getPrimaryPrincipal());
        Session kickOutSession;
        Integer sessionId = (Integer) session.getId();
        Object kickOutSessionId = null;
        User user = new User();
        try {
            BeanUtils.copyProperties(user, info.getPrincipals().getPrimaryPrincipal());
            REDIS_LIST_KEY = user.getUsername();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        //第一次登录,从当前session中获取KICK_OUT_KEY值
        if (session.getAttribute(KICK_OUT_KEY) == null && REDIS_LIST_KEY != null) {
            redisList.leftPush(REDIS_LIST_KEY, sessionId);
        }

        //队列里面超过MAX_LOGIN_NUM，开始踢人
        //enbleKickOut也算一个扩展点叭，默认是true，可以踢人，当然自己也可以进行扩展设置
        if (redisList.size(REDIS_LIST_KEY) > MAX_LOGIN_NUM && enbleKickOut) {
            if (kickOutOrder) {
                //踢出，返回值是踢出的value
                kickOutSessionId = redisList.rightPop(REDIS_LIST_KEY);
            } else {
                kickOutSessionId = redisList.leftPop(REDIS_LIST_KEY);
            }
        }
        try {
            //获取被踢出用户的session
            kickOutSession = sessionManager.getSession(new DefaultSessionKey((Serializable) kickOutSessionId));
            kickOutSession.setAttribute(KICK_OUT_KEY, true);
            log.info("踢出用户成功");
        } catch (Exception e) {
            log.info("踢出用户出错");
        }
        log.info("监听登录成功");
    }

    public KickOutListener setEnbleKickOut(Boolean enbleKickOut) {
        this.enbleKickOut = enbleKickOut;
        return this;
    }

    /**
     * @param
     * @method shiro登录失败后调用
     */
    @Override
    public void onFailure(AuthenticationToken token, AuthenticationException ae) {
        log.info("监听登录失败");
    }

    /**
     * @param
     * @method shiro登出后调用
     */
    @Override
    public void onLogout(PrincipalCollection principals) {
        log.info("监听登出成功");
    }
}
