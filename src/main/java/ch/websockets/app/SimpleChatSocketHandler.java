package ch.websockets.app;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SimpleChatSocketHandler extends TextWebSocketHandler {

  class Registration {
    String type;
    String username;
  }

  List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
  HashMap<String, WebSocketSession> users = new  HashMap<>();

  @Override
  public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
    System.out.println("Received message: " + message.getPayload());
    List<WebSocketSession> brokenSessions = new CopyOnWriteArrayList<>();
    
    if (message.getPayload().contains("\"type\":\"register\"")) {
      Registration reg = new Gson().fromJson(message.getPayload(), Registration.class);
      if (users.containsKey(reg.username)){
        //Raise error
        try {
          session.sendMessage(new TextMessage("Username already taken, pls choose another name"));
        } catch (IOException e) {
          e.printStackTrace();
        }
        finally {
          brokenSessions.add(session);
        }
      } 
      else {
        users.put(reg.username, session);        
      }
      return;
    }

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
    sendUserListUpdate();
  }

  private void sendUserListUpdate(){
      Set<String> tUsernames = users.keySet();
      JsonArray usernames = new JsonArray();
      tUsernames.forEach(user -> usernames.add(user));
      JsonObject payload = new JsonObject();
      payload.addProperty("type", "userlist");
      payload.add("users", usernames);
      TextMessage message = new TextMessage(payload.toString().getBytes(Charset.defaultCharset()));
      sessions.forEach(session -> {
        try {
          session.sendMessage(message);
        }
        catch(Exception ioex){
          System.out.println("uupsi... crashed on sending user list updates");
        }
      });   
      
  }

  @Override
  public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
    sessions.add(session);
  }

}
