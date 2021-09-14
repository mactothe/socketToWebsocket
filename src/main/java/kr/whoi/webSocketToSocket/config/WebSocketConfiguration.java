package kr.whoi.webSocketToSocket.config;

import kr.whoi.webSocketToSocket.handler.WebSocketServerHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import static kr.whoi.webSocketToSocket.App.WEBSOCKET_PATH;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    private WebSocketServerHandler webSocketHandler;

    public WebSocketConfiguration(WebSocketServerHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(webSocketHandler, WEBSOCKET_PATH).setAllowedOrigins("*");
    }
}
