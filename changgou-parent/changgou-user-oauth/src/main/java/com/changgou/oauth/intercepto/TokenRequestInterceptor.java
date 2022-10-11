package com.changgou.oauth.intercepto;

import com.changgou.oauth.util.AdminToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenRequestInterceptor implements RequestInterceptor {


    @Override
    public void apply(RequestTemplate template){
        String  token = AdminToken.adminToken();
        template.header("Authorization","bearer "+token);
    }
}
