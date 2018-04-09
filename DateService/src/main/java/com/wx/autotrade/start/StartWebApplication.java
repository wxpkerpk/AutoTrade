package com.wx.autotrade.start;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.wx.autotrade.mapper")
//@EntityScan(value = "com.wx.cloudprint.dataservice.entity")
//@ComponentScan({"com.wx.cloudprint"})
//@EnableJpaRepositories(basePackages = "com.wx.cloudprint.dataservice.dao")
public class StartWebApplication {

    public static void main(String[] args) {


        SpringApplication.run(StartWebApplication.class, args);

    }


}
