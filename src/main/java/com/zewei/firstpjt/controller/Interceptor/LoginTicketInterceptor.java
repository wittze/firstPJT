package com.zewei.firstpjt.controller.Interceptor;

import com.zewei.firstpjt.entity.LoginTicket;
import com.zewei.firstpjt.entity.User;
import com.zewei.firstpjt.service.UserService;
import com.zewei.firstpjt.util.CookieUtil;
import com.zewei.firstpjt.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");

        if (ticket != null) {
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // controller之前检查是否依然有效：查到的不为空、状态为0、过期时间晚于当前时间
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                // 有效，认为存在，根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次请求中持有用户，通过存到当前线程实现
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    // Controller 方法调用之后执行，但是它会在 DispatcherServlet 进行视图返回渲染之前被调用
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    //整个请求结束之后，也就是在 DispatcherServlet 渲染了对应的视图之后执行。此方法主要用来进行资源清理。
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
