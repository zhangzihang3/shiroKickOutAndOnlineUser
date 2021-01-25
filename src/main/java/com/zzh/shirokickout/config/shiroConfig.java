package com.zzh.shirokickout.config;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import com.zzh.shirokickout.auth.realm.loginRealm;
import com.zzh.shirokickout.cache.shiroSessionCache;
import com.zzh.shirokickout.filters.kickOutFilter;
import com.zzh.shirokickout.listeners.KickOutListener;
import com.zzh.shirokickout.listeners.ShiroSessionListener;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.event.support.DefaultEventBus;
import org.apache.shiro.mgt.AuthenticatingSecurityManager;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.LinkedHashMap;

@Configuration
public class shiroConfig {
    @Autowired
    @Qualifier("redisCacheManager")
    private RedisCacheManager redisCacheManager;
    @Autowired
    @Qualifier("shiroCacheManager")
    private com.zzh.shirokickout.cache.shiroCacheManager shiroCacheManager;
    @Autowired
    private SessionManager sessionManager;

    /**
     * perms 指定的权限都要有才能访问
     * roles  指定的角色都要有才能访问
     * user 登录且访问对应的url有相应的权限角色才能访问
     * authc 必须认证了才能访问（进行鉴权）
     * anon 无需任何权限或者角色就可以访问
     *
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean() {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        //这里一定要用LinkedHashMap避免设置的过滤规则乱序
        LinkedHashMap<String, String> chain = new LinkedHashMap<String, String>();
        //设置拦截器
        LinkedHashMap<String, Filter> filters = new LinkedHashMap<>();
        filters.put("kickOutFilter", new kickOutFilter().setKICK_OUT_URL("/kickOut"));
        //这些路径下的资源不需要权限
        chain.put("/kickOut/login", "anon"); // 登录链接不拦截
        chain.put("/css/**", "anon");
        chain.put("/img/**", "anon");
        chain.put("/js/**", "anon");
        chain.put("/lib/**", "anon");
        chain.put("/needQuanXian", "roles[index]");
        //其他路径需要认证，并且会经过kickOutFilter过滤，被这个一定要写在最后，这样所有的过滤规则才会生效
        chain.put("/**", "authc,kickOutFilter,noSessionCreation");
        //设置资源访问权限
        bean.setFilterChainDefinitionMap(chain);
        bean.setSecurityManager(securityManager());
        //设置没有登录时的默认跳转页面
        bean.setLoginUrl("/login");
        //登录了但是没有相应权限时的跳转页面
        bean.setUnauthorizedUrl("/unAuth");
        bean.setFilters(filters);
        return bean;
    }


    /**
     * @param
     * @method 授权
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        // 注入安全管理器
        advisor.setSecurityManager(securityManager());
        return advisor;
    }

    /**
     * @param
     * @method 这里设置了shiroCacheManager，表示要缓存loginRealm,以及会话session。
     * 这里如果不设置shiroCacheManager，由于shiroSessionDao(shiroCacheManager)已经注入过shiroCacheManager
     * session信息也是能被缓存的
     */

    @Bean
    public SecurityManager securityManager() {
        ArrayList<AuthenticationListener> listeners = new ArrayList<>();
        ModularRealmAuthenticator modularRealmAuthenticator = new ModularRealmAuthenticator();
        //添加监听器
        listeners.add(kickOutListener());
        modularRealmAuthenticator.setAuthenticationListeners(listeners);

        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 缓存授权信息，也就是realm
        // securityManager.setCacheManager(shiroCacheManager);
        securityManager.setSessionManager(sessionManager());
        //这里切记先添加modularRealmAuthenticator，在添加loginRealm()，否则报错
        //Configuration error: No realms have been configured! One or more realms must be present to execute an authorization operation.java.lang.IllegalStateException: Configuration error:  No realms have been configured!  One or more realms must be present to execute an authorization operation.
        securityManager.setAuthenticator(modularRealmAuthenticator);
        securityManager.setRealm(loginRealm());
        return securityManager;
    }

    /**
     * @param
     * @method 配置kickOutListener bean
     */
    @Bean
    public KickOutListener kickOutListener() {
        return new KickOutListener();
    }

    /**
     * @param
     * @method 配置ShiroSessionListener bean
     */
    @Bean
    public ShiroSessionListener ShiroSessionListener() {
        return new ShiroSessionListener();
    }


    /**
     * @param
     * @method 配置loginRealm bean
     */
    @Bean
    public loginRealm loginRealm() {
        return new loginRealm();
    }


    /**
     * @param
     * @method 配置AuthenticationListener bean
     */
    @Bean
    public AuthenticationListener KickOutListener() {
        return new KickOutListener();
    }

    /**
     * @param
     * @method DefaultWebSessionManager的配置，设置了sessionDAO
     */
    @Bean
    public SessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        ArrayList<SessionListener> listeners = new ArrayList<>();
        listeners.add(ShiroSessionListener());
        sessionManager.setSessionListeners(listeners);
        sessionManager.setSessionDAO(sessionDAO());
        return sessionManager;
    }

    /**
     * @param
     * @method 这里配置shiro的会话底层的缓存管理器，底层用的是redis缓存会话
     */
    @Bean
    public SessionDAO sessionDAO() {
        shiroSessionCache shiroSessionDao = new shiroSessionCache(shiroCacheManager);
        return shiroSessionDao;
    }

    /**
     * 启用shiro thymeleaf标签支持
     *
     * @return
     */
    @Bean
    public ShiroDialect shiroDialect() {
        return new ShiroDialect();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor
                = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAAP = new DefaultAdvisorAutoProxyCreator();
        defaultAAP.setProxyTargetClass(true);
        return defaultAAP;
    }
}
