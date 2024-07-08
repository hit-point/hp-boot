package com.hpboot;

import cn.dev33.satoken.secure.BCrypt;
import org.junit.Test;

public class HpbootApplicationTests {

    @Test
    public void contextLoads() {
    }

    public static void main(String[] args) {
        String hashpw = BCrypt.hashpw("ry123");
        System.out.println(hashpw);

        System.out.println(BCrypt.checkpw("123", hashpw));
    }

}
