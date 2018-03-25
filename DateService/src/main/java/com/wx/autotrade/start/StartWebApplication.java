package com.wx.autotrade.start;

import com.wx.autotrade.websocket.WebSocketService;
import com.wx.autotrade.websocket.test.BuissnesWebSocketServiceImpl;
import com.wx.autotrade.websocket.test.WebSoketClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//@EntityScan(value = "com.wx.cloudprint.dataservice.entity")
//@ComponentScan({"com.wx.cloudprint"})
//@EnableJpaRepositories(basePackages = "com.wx.cloudprint.dataservice.dao")
public class StartWebApplication {

    public static void main(String[] args) {


        SpringApplication.run(StartWebApplication.class, args);
        // apiKey 为用户申请的apiKey
        String apiKey  ;

        // secretKey为用户申请的secretKey
        String secretKey  ;

        apiKey = "ef63c6bb-463b-47dc-99d6-b016120fbd7d";
        secretKey = "6BA2A61B50CFFA00EA8B8F87C3612168";

        // 国际站WebSocket地址 注意如果访问国内站 请将 real.okcoin.com 改为 real.okcoin.cn
        String url = "wss://real.okex.com:10440/websocket/okexapi";

        // 订阅消息处理类,用于处理WebSocket服务返回的消息
        WebSocketService service = new BuissnesWebSocketServiceImpl();

        // WebSocket客户端
        WebSoketClient client = new WebSoketClient(url, service);

        // 启动客户端
        client.start();

        // 添加订阅
        client.addChannel("ok_sub_spot_bch_btc_kline_1min");
    }


}
