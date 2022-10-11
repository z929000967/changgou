package com.changgou.token;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * 令牌的创建和解析
 */
public class CreateJwtTest {

    /**
     * 创建令牌
     */
    @Test
    public void testCreateToken(){
        //加载证书
        ClassPathResource resource = new ClassPathResource("changgou.jks");

        //获取证书数据
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,"changgou".toCharArray());

        //获取证书中的一对密钥
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair("changgou", "changgou".toCharArray());

        //获取私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();


        //创建令牌，需要私钥加盐
        Map<String,Object> payload = new HashMap<String, Object>();
        payload.put("name","tomcat");
        payload.put("address","sz");
        payload.put("authorities",new String[]{"admin","oauth"});

        Jwt jwt = JwtHelper.encode(JSON.toJSONString(payload), new RsaSigner(privateKey));

        //获取令牌数据
        String token = jwt.getEncoded();
        System.out.println(token);

    }

    /**
     * 解析令牌
     */
    @Test
    public void testParseToken(){
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZGRyZXNzIjoic3oiLCJuYW1lIjoidG9tY2F0IiwiYXV0aG9yaXRpZXMiOlsiYWRtaW4iLCJvYXV0aCJdfQ.WValUf2Sa37vjsRdk-pAD397PyuFDSa8gXnpWADUyN9bzWjv_Puu85U_-qQ5Y8VbcTEXi-NfmCc_PyeKG2uwJpx0PQSGV-HPOy8-at5L6_d379pl8ihfu_X9YSGdNpMH_FDMQtl6cedqHoQn9Yls28uTGipl6exbyIywb29xNtzPHOp329S33efbIYQPT4fvRemoTViESTsaaTut4NgyHQFbm1jI3JrtBW5u3Ha0n-rHCuxy9zPqR2ci1nRKsYa2kn8io4DRpfUXfZgAa30qYR9Q8QMsoM1UOk7xqkRGOy2M3U_pZtF-gm1u0DNjZ4zGVJ5Nym8wYF0-5ogtBrAm-g";
        String pubilckey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwHlC09OB81OyQNOFJehYFOOH2BgGIQZERmNmOHEHA1xUb+2SoqqfgP2ec9+y9LAhD5W4nXy72c9ZPrNX+cHpxk3h6XQ+if8j6QO8z/+EDzY96N1PXq8fkW1mhjkhmCjgfs/fq6c8HUewxgJOVLE1wrOXB7ok1SPXbsHM6eRmSY5yEMYDIjVYI0EfkWUOfcXwUXNJGMLhYQlHyyRDz4fQ0pHsK9SHVSvAF0fxTZwdetikWF8HW7Dtl5BY/tCliWRSpcDnQ3sVRygFBKZK/uZp5RTaFzTih84ZKy317IcIP1T+1k1bCKHJ6i3vkRPBJTyHtsJ5GvQcdnSw6QjQ4KayvQIDAQAB-----END PUBLIC KEY-----";
        Jwt jwt = JwtHelper.decodeAndVerify(token,
                new RsaVerifier(pubilckey)
        );
        String claims = jwt.getClaims();
        System.out.println(claims);
    }


}
