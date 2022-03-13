package com.zewei.firstpjt.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密,不能解密
    // hello -> abc123def456 容易被使用密码库破解
    // hello + 3e4a8 -> abc123def456abc 再原密码后拼接随机字符串再转成md5避免上述问题
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

}
