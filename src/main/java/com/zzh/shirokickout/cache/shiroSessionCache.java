package com.zzh.shirokickout.cache;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

import static java.lang.System.out;

/**
 * @author 张子行
 * @class 自定义CachingSessionDAO
 */
@Slf4j
public class shiroSessionCache extends CachingSessionDAO {
    private static Serializable sessionId = null;
    private String SESSION_PREFIX = "shiro-activeSessionCache::";
    private String SESSION_CACHE = "shiro-activeSessionCache";


    /**
     * @param
     * @method 提供缓存session的扩展方法，到时候用的时候，
     * 想用什么缓存session信息，直接注入对应的CacheManager(shiro)就行
     */
    public shiroSessionCache(CacheManager cacheManager) {
        super.setCacheManager(cacheManager);
    }

    @Override
    protected void doUpdate(Session session) {
//        CacheManager cacheManager = super.getCacheManager();
//        cacheManager.getCache(SESSION_CACHE).put(session.getId(), sessionToByte(session));
//        log.info("shiroSessionCache更新session" + session.getId());
    }

    @Override
    protected void doDelete(Session session) {
//        CacheManager cacheManager = super.getCacheManager();
//        try {
//            cacheManager.getCache(SESSION_CACHE).remove(SESSION_PREFIX + session.getId());
//        } catch (Exception e) {
//            log.warn("删除session失败---shiro-activeSessionCache::" + session.getId());
//            e.printStackTrace();
//        }
        log.info("删除session成功---shiro-activeSessionCache::" + session.getId());
    }

    @Override
    protected Serializable doCreate(Session session) {
        session.setTimeout(3000);
        sessionId = new Random().nextInt(1000000);
        assignSessionId(session, sessionId);
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        CacheManager cacheManager = super.getCacheManager();
        Session session = null;
//        try {
//            session = (Session) cacheManager.getCache(SESSION_CACHE).get(SESSION_PREFIX + sessionId);
//        } catch (Exception e) {
//            log.warn("读取session失败---shiro-activeSessionCache::" + sessionId);
//            e.printStackTrace();
//        }
//        log.info("读取session成功---shiro-activeSessionCache::" + sessionId);
        return session;
    }

    /**
     * @param
     * @method 把session对象转化为byte保存到redis中
     */
    public byte[] sessionToByte(Session session) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] bytes = null;
        try {
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(session);
            bytes = bo.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }
}
