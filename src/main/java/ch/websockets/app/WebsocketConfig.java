package ch.websockets.app;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.*;


@Configuration
@EnableWebSocket
public class WebsocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(new SimpleChatSocketHandler(), "/chat").setAllowedOrigins("*");
        registry.addHandler(new TimeSocketHandler(), "/time").setAllowedOrigins("*");
    }
}
