package com.zhangyuan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.modulith.Modulith;

@Modulith(sharedModules = {"common"})
@SpringBootApplication
@EnableFeignClients
public class ZhangyuanApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhangyuanApplication.class, args);
    }
}
