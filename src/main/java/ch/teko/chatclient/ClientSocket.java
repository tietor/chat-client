package ch.teko.chatclient;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import ch.teko.chatclient.server.Command;
import ch.teko.chatclient.server.Server;

import javafx.scene.control.TextArea;

public class ClientSocket {

  private final int destinationPort;

  private Socket serverSocketResponse;

  public static final String END_OF_LINE = "\n";

  private boolean isRunning = true;

  public ClientSocket(int destinationPort) {
    this.destinationPort = destinationPort;
  }

  public void connectToServerWithUsername(String username) {
    Thread connectToServer = new Thread(() -> {
      try {
        System.out.println("Connecting to server...");
        this.serverSocketResponse = new Socket(InetAddress.getByName("127.0.0.1"), destinationPort);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(serverSocketResponse.getOutputStream()));
        writer.write(Command.REGISTER + "#" + username + END_OF_LINE);
        writer.flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    connectToServer.start();
  }

  public void fetchMessagesEverySecond(TextArea chatHistory) {
    Thread fetchMessages = new Thread(() -> {
      while (isRunning) {
        requestChatMessages();
        List<Message> messages = getChatMessages();
        chatHistory.clear();
        for (Message message : messages) {
          System.out.println();
          chatHistory.appendText(message.getUsername() + ": " + message.getMessage() + END_OF_LINE);
        }
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    });
    fetchMessages.start();
  }

  private ArrayList<Message> getChatMessages() {
    try {
      System.out.println("Waiting for chat messages...");
      ObjectInputStream objectInputStream = new ObjectInputStream(serverSocketResponse.getInputStream());
      System.out.println("Chat messages received");
      return (ArrayList<Message>) objectInputStream.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private void requestChatMessages() {
    sendMessageWithCommand(Command.GET, String.valueOf(Server.DEFAULT_AMOUNT_OF_MESSAGES));
  }

  public void sendExitMessage() {
    isRunning = false;
    try {
      System.out.println("sending exit command");
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(serverSocketResponse.getOutputStream()));
      writer.write(Command.EXIT + END_OF_LINE);
      writer.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void sendMessageWithCommand(Command command, String message) {
    System.out.println("sending " + command.name() + " command with message: " + message);
    try {
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(serverSocketResponse.getOutputStream()));
      writer.write(command.name() + "#" + message + END_OF_LINE);
      writer.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
