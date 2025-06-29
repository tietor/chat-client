package ch.teko.chatclient;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class HelloController {

  @FXML
  private TextField username;

  @FXML
  private TextArea chatHistory;

  @FXML
  private TextField message;

  @FXML
  private Label errorMessage;

  private static ClientSocket clientSocket;

  @FXML
  protected void connectToServer() throws InterruptedException {
    clientSocket = new ClientSocket(1200);
    clientSocket.connectToServerWithUsername(username.getText());
    errorMessage.setText("");
    Thread.sleep(1000);
    clientSocket.fetchMessagesEverySecond(chatHistory);
  }

  @FXML
  protected void sendMessage() {
    if (isUserConnected()) {
      clientSocket.sendMessage(message.getText());
    } else {
      errorMessage.setText("Please connect to the server");
    }

  }

  private static boolean isUserConnected() {
    return clientSocket != null;
  }

  public static void closeSocket() {
    if (isUserConnected()) {
      clientSocket.sendExitMessage();
    }
  }
}