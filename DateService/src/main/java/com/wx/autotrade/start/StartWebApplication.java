package com.wx.autotrade.start;


import com.wx.autotrade.service.DataCollectService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.wx.autotrade.mapper")

//@EntityScan(value = "com.wx.cloudprint.dataservice.entity")
@ComponentScan({"com.wx"})
//@EnableJpaRepositories(basePackages = "com.wx.cloudprint.dataservice.dao")
public class StartWebApplication {

    public static void main(String[] args) {

        SpringApplication springApplication = new SpringApplication(StartWebApplication.class);
        springApplication.addListeners(new SpringUtils());
        springApplication.run(args);
        DataCollectService.startCollectData();

    }


}
