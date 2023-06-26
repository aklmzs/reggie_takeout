package com.it.reggie.filter;


import com.alibaba.fastjson.JSON;
import com.it.reggie.common.BaseContext;
import com.it.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;



@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器
    public static final AntPathMatcher PATH_MATCHER =new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;

        String requestURI = request.getRequestURI();
        //定义不需要处理的路径
        String[] urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/login",
                "/user/sendMsg"

        };
        boolean check = check(urls, requestURI);
        if (check){
            filterChain.doFilter(request,response);
            return;
        }
        //判断登录状态
        if(request.getSession().getAttribute("employee")!=null){

            Long empId =(Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }

        if(request.getSession().getAttribute("user")!=null){

            Long userId =(Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }
        //若未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }
    public boolean check(String[] urls, String requestURI){
        for (String url:urls){
            boolean match=PATH_MATCHER.match(url,requestURI);
            if (match){
                return true;
            }
        }
        return false;

    }
}
