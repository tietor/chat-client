package ch.teko.chatclient;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ChatClientApplication extends Application {

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(ChatClientApplication.class.getResource("chat-view.fxml"));
    Scene scene = new Scene(fxmlLoader.load(), 300, 180);
    stage.setTitle("Chatapplication");
    stage.setResizable(false);
    stage.setScene(scene);
    stage.show();
    scene.getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
      ChatClientController.closeSocket();
    });
  }

  public static void main(String[] args) {
    launch();
  }
}