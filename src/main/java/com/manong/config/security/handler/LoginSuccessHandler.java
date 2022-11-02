package com.manong.config.security.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.manong.config.redis.RedisService;
import com.manong.entity.User;
import com.manong.utils.JwtUtils;
import com.manong.utils.LoginResult;
import com.manong.utils.ResultCode;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Resource
    private JwtUtils jwtUtils;
    @Resource
    private RedisService redisService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //设置响应的编码格式
        response.setContentType("application/json;charset=UTF-8");
        //获取输出流
        ServletOutputStream outputStream = response.getOutputStream();
        //获取当前登录用户信息
        User user = (User) authentication.getPrincipal();
        //生成token
        String token = jwtUtils.generateToken(user);
        //把生成的token存到redis
        String tokenKey = "token_"+token;
        redisService.set(tokenKey,token,jwtUtils.getExpiration() / 1000);
        //设置TOKEN的签名密钥和过期时间
        long expireTime = Jwts.parser()
                .setSigningKey(jwtUtils.getSecret())        //设置签名密钥
                .parseClaimsJws(token.replace("jwt_",""))
                .getBody().getExpiration().getTime();       //设置过期时间
        //创建LoginResult登陆结果对象
        LoginResult loginResult = new LoginResult(user.getId(), ResultCode.SUCCESS, token,expireTime);
        //将对象转化成JSON格式，消除循环引用
        String result = JSON.toJSONString(loginResult, SerializerFeature.DisableCircularReferenceDetect);
        outputStream.write(result.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
    }
}
