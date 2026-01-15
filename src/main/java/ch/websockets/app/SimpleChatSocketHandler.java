package ch.websockets.app;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class SimpleChatSocketHandler extends TextWebSocketHandler {

  List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

  @Override
  public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
    System.out.println("Received message: " + message.getPayload());
    List<WebSocketSession> brokenSessions = new CopyOnWriteArrayList<>();
    for (WebSocketSession s: sessions) {
      try {
        if (s.isOpen()) {
          s.sendMessage(message);
        } else {
          brokenSessions.add(s);
        }
      } catch(Exception ex){
        brokenSessions.add(s);
      }
    }
    sessions.removeAll(brokenSessions);
  }

  @Override
  public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
    sessions.add(session);
  }

}
