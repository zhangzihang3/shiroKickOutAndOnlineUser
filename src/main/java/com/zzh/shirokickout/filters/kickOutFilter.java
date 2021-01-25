package com.zzh.shirokickout.filters;

import com.zzh.shirokickout.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebListener;

import static java.lang.System.out;

/**
 * @author 张子行
 * @class 自己在学习shiro框架时, 用户在登录的时候只执行认证方法而没有去执行授权doGetAuthorizationInfo()方法.刚开始以为是哪里配置错误了, 有人说shiro的东西要配到SpringMVC的配置文件中, 结果发现并没有什么用.后来才发现对shiro认证与授权理解错误.
 * shiro并不是在认证之后就马上对用户授权,而是在用户认证通过之后,接下来要访问的资源或者目标方法需要权限的时候才会调用doGetAuthorizationInfo()方法,进行授权.
 * 比如当认证通过后,访问@RequiresPermissions注解的目标方法,或者目标页面中有shiro的权限标签,这是shiro就会调用doGetAuthorizationInfo()方法.
 */
@Slf4j
public class kickOutFilter extends AccessControlFilter {
    private String KICK_OUT_URL;
    private String KICK_OUT_KEY = "ZZH_KICK_OUT";

    public kickOutFilter setKICK_OUT_URL(String KICK_OUT_URL) {
        this.KICK_OUT_URL = KICK_OUT_URL;
        return this;
    }

    /**
     * @param
     * @method return false接着进入onAccessDenied，反之不会
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        log.info("isAccessAllowed");
        return false;
    }

    /**
     * @param
     * @method return false直接给浏览器响应，return true正常请求
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        log.info("onAccessDenied");
        Subject subject = getSubject(servletRequest, servletResponse);
        User user = new User();
        Session session;
        try {
            BeanUtils.copyProperties(user,subject.getPrincipals().getPrimaryPrincipal());
            if (!subject.isAuthenticated() && !subject.isRemembered()) {
                //如果没有登录，不做处理直接放行
                return true;
            }
        } catch (Exception e) {
            System.out.println(e);
            //可能此次操作之前并没有进行过登录，那么subject.getPrincipals().getPrimaryPrincipal()此时为null会出现异常，直接放行就行
            return true;
        }
        //当前登录的subject的session
        session = subject.getSession();
        if (user == null || session == null) {
            //如果此次操作的subject中都没有userName、session的值，也不做处理直接放行
            return true;
        }
        if (session.getAttribute(KICK_OUT_KEY) != null) {
            subject.logout();

            //此次会话已经被踢出，跳转到踢出页面
            WebUtils.issueRedirect(servletRequest, servletResponse, KICK_OUT_URL);
            return false;
        }
        return true;
    }
}
