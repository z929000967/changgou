package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

public interface AuthService {

    /***
     * 授权认证方法
     */
    AuthToken login(String username, String password, String clientId, String clientSecret);
}
