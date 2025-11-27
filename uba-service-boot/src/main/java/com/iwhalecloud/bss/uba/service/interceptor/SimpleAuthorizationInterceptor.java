package com.iwhalecloud.bss.uba.service.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import com.iwhalecloud.bss.magic.magicapi.core.context.MagicUser;
import com.iwhalecloud.bss.magic.magicapi.core.exception.MagicLoginException;
import com.iwhalecloud.bss.magic.magicapi.core.interceptor.Authorization;
import com.iwhalecloud.bss.magic.magicapi.core.interceptor.AuthorizationInterceptor;
import com.iwhalecloud.bss.magic.magicapi.core.servlet.MagicHttpServletRequest;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 自定义多用户名密码登录，以及权限控制
 * 使用方式在项目工程中@import 或者 @Bean注解使spring管理。
 * magic-api.ext.auth.enable=true #启用
 * #magic-api.ext.auth.users.用户名=123456  #配置用户信息
 * #magic-api.ext.auth.denyOptions.用户名=DELETE #配置用户禁用的权限 {@linkplain Authorization}
 * #magic-api.ext.auth.users.xx=password
 * #magic-api.ext.auth.denyOptions.xx=DELETE
 * @author 冰点
 * @date 2021-5-11 17:17:52
 */

@Configuration
@ConditionalOnProperty(prefix = "magic-api.ext.auth", name = "enable", havingValue = "true", matchIfMissing = false)
@ConfigurationProperties(prefix = "magic-api.ext.auth")
public class SimpleAuthorizationInterceptor implements AuthorizationInterceptor {
    private static final Logger log = LoggerFactory.getLogger(SimpleAuthorizationInterceptor.class);
    /**
     * 加密因子
     */
    @Value("${magic-api.ext.auth.encryIndex:1}")
    private int encryIndex;
    /**
     * 用户信息
     */
    private Map<String, String> users;
    /**
     * 用户权限
     */
    private Map<String, String> denyOptions;


    public SimpleAuthorizationInterceptor() {
        log.info("Multi-user login extension is enabled. To disable it, please set magic-api.ext.auth.enable=false");
    }

    /**
     * 配置是否需要登录
     */
    @Override
    public boolean requireLogin() {
        return true;
    }

    /**
     * 根据Token获取User
     */
    @Override
    public MagicUser getUserByToken(String token) throws MagicLoginException {
        try {
            String[] userInfo = getUserInfoByToken(token);
            MagicUser magicUser = new MagicUser(userInfo[0], userInfo[0], getToken(userInfo[0], userInfo[1]));
            if (users.containsKey(magicUser.getUsername()) && users.get(magicUser.getUsername()).equals(userInfo[1])) {
                return magicUser;
            }
        } catch (Exception e) {
            log.error("Invalid token, please log in again. If you still have problems, you can manually clear the magic-token in localStorage");
        }
        throw new MagicLoginException("Invalid token");
    }

    @Override
    public MagicUser login(String username, String password) throws MagicLoginException {
        // 根据实际情况进行修改。。
        if (users.containsKey(username) && users.get(username).equals(password)) {
            return new MagicUser(username, username, getToken(username, password));
        }
        throw new MagicLoginException("Incorrect username or password");
    }

    /**
     * 验证是否有权限访问功能
     */
    @Override
    public boolean allowVisit(MagicUser magicUser, MagicHttpServletRequest request, Authorization authorization) {
        if(denyOptions==null){
            return true;
        }
        String[] denyOption = denyOptions.get(magicUser.getUsername()).split(",");
        List<String> list = Arrays.asList(denyOption);
        return !list.contains(authorization.name());
    }

    public String getToken(String username, String password) throws MagicLoginException {
        String token = null;
        try {
            byte[] b = (username + ";" + password).getBytes("utf-8");
            for (int i = 0; i < b.length; i++) {
                b[i] += encryIndex;
            }
            token = new String(b);
            log.debug("Login token for this session: [{}]", token);
        } catch (UnsupportedEncodingException e) {
            log.info("Failed to generate token, possibly due to an invalid character set. [{}={}]",username,password);
            throw new MagicLoginException("Invalid username or password configuration");
        }
        return token;
    }

    public String[] getUserInfoByToken(String token) throws MagicLoginException {
        try {
            byte[] b = token.getBytes();
            for (int i = 0; i < b.length; i++) {
                b[i] -= encryIndex;
            }
            return new String(b).split(";");
        } catch (Exception e) {
            log.error("Failed to retrieve user information based on token: [{}]", token, e);
            throw new MagicLoginException("Incorrect username or password");
        }
    }

    public void setUsers(Map<String, String> users) {
        this.users = users;
    }

    public void setDenyOptions(Map<String, String> denyOptions) {
        this.denyOptions = denyOptions;
    }
}
