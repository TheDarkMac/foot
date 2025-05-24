package com.fifa.app.Configuration;

import com.fifa.app.Security.ApiSecurity;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

public class ApiFilterConfiguration {
    @Bean
    public FilterRegistrationBean<ApiSecurity> filterRegistrationBean(ApiSecurity apiSecurity) {
        FilterRegistrationBean<ApiSecurity> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(apiSecurity);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("apiFilter");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
