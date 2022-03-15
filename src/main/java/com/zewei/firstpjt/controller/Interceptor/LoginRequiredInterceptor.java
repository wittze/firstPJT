package com.zewei.firstpjt.controller.Interceptor;


import com.zewei.firstpjt.annotation.LoginRequired;
import com.zewei.firstpjt.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断拦截的是不是方法，静态资源放行
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            //从该方法上拿到注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            //有这个注解并且没登录
            if (loginRequired != null && hostHolder.getUser() == null) {
                //需要登录，response重定向
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
