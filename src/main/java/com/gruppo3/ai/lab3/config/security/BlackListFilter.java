package com.gruppo3.ai.lab3.config.security;

import com.gruppo3.ai.lab3.service.TokenBlackListService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BlackListFilter extends GenericFilterBean{

    private TokenBlackListService myTokenBlackListService;
    public String url = "";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String url = "";

        ServletContext servletContext = servletRequest.getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        myTokenBlackListService = webApplicationContext.getBean(TokenBlackListService.class);

        if (servletRequest instanceof HttpServletRequest) {
            url = ((HttpServletRequest)servletRequest).getRequestURL().toString();
            // String queryString = ((HttpServletRequest)servletRequest).getQueryString();
            // System.out.println("qqq: " + url);
        }

        if(!url.contains("error")) {
            int tokenResponse = myTokenBlackListService.getFromBlackList();
            switch(tokenResponse) {
                case -1: // nessun token presentato
                    if (!url.contains("enrollment")) { // api request
                        System.out.println("nessun token presentato, api request -> nega accesso");
                        HttpServletResponse response = (HttpServletResponse) servletResponse;
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Api request denied");
                    } else {
                        // enrollment request
                        System.out.println("nessun token presentato, enrollment request -> consenti accesso");
                        filterChain.doFilter(servletRequest, servletResponse);
                    }
                    break;
                case 0: // il token presentato è nella lista blacktoken
                    if (!url.contains("enrollment")) { // api request
                        System.out.println("token presentato blackato, api request -> nega accesso");
                        HttpServletResponse response = (HttpServletResponse) servletResponse;
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Api request denied");
                    }
                    else { // enrollment request
                        System.out.println("token presentato blackato, enrollment request -> consenti accesso");
                        filterChain.doFilter(servletRequest, servletResponse);
                    }
                    break;
                case 1: // il token presentato non è nella lista blacktoken
                    if (!url.contains("enrollment")) { // api request
                        System.out.println("token presentato non blackato, api request -> consenti accesso");
                        filterChain.doFilter(servletRequest, servletResponse);
                    }
                    else { // enrollment request
                        System.out.println("token presentato non blackato, enrollment request -> nega accesso");
                        HttpServletResponse response = (HttpServletResponse) servletResponse;
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Enrollment request denied");
                    }
                    break;
            }
        }
    }
}