package com.wx.autotrade.websocket.test;


import com.wx.autotrade.websocket.WebSocketBase;
import com.wx.autotrade.websocket.WebSocketService;
/**
 * 通过继承WebSocketBase创建WebSocket客户端
 * @author okcoin
 *
 */
public class WebSoketClient extends WebSocketBase{
	public WebSoketClient(String url,WebSocketService service){
		super(url,service);
	}
}
