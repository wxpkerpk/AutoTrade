package com.wx.autotrade.websocket.test;

import org.apache.log4j.Logger;

import com.wx.autotrade.websocket.WebSocketBase;
import com.wx.autotrade.websocket.WebSocketService;
/**
 * 订阅信息处理类需要实现WebSocketService接口
 * @author okcoin
 *
 */
public class BuissnesWebSocketServiceImpl implements WebSocketService{
	private Logger log = Logger.getLogger(WebSocketBase.class);
	@Override
	public void onReceive(String msg){
		
		log.info("WebSocket Client received message: " + msg);
	
	}
}
