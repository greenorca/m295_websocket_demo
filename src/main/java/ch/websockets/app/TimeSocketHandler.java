package ch.websockets.app;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

  private ScheduledExecutorService scheduler;

  public TimeSocketHandler() {
    this.scheduler = Executors.newSingleThreadScheduledExecutor();

    scheduler.scheduleAtFixedRate(() -> {
        String time = Instant.now().toString().substring(0, 19).replace("T", " ");
        sessions.removeIf(session ->
            !session.isOpen() || !sendMessage(session, time)
        );
    }, 0, 1, TimeUnit.SECONDS);

  }

  @Override
  public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message){
    logger.info("Received message: {}", message.getPayload());
  }

  @Override
  public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
    sessions.add(session);
  }
}
