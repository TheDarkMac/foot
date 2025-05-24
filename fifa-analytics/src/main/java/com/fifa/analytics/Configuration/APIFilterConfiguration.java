package com.fifa.analytics.Configuration;

import com.fifa.analytics.Security.ApiSecurity;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class APIFilterConfiguration {

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
