package ch.websockets.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class TimeSocketHandler extends TextWebSocketHandler {

  List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

  Logger logger = LoggerFactory.getLogger(TimeSocketHandler.class);

  private boolean sendMessage(WebSocketSession session, String message) {
    if (!session.isOpen()){
      return false;
    }
    try {
      session.sendMessage(new TextMessage(message));
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public TimeSocketHandler() {
    new Thread(() -> {
      while(true) {
        try {
          String time = Calendar.getInstance().getTime().toString();
          List<WebSocketSession> brokeSessions = new ArrayList<>();
          for(WebSocketSession session : sessions) {
            if (!sendMessage(session, time)) {
              brokeSessions.add(session);
            }
          }
          sessions.removeAll(brokeSessions);
          Thread.sleep(1000);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }).start();
  }

  @Override
  public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message){
    try {
      StringBuilder sb = new StringBuilder();
      sb.append("Received message: ");
      sb.append(message.getPayload());
      logger.info(sb.toString());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
    sessions.add(session);
  }
}
