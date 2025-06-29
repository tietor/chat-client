package ch.teko.chatclient.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import ch.teko.chatclient.Command;
import ch.teko.chatclient.Message;

public class Server {

  private ServerSocket serverSocket;
  private final List<Message> messageHistory = new ArrayList<>();
  private Semaphore semaphore;
  public static final int DEFAULT_AMOUNT_OF_MESSAGES = 50;
  public static final int MAX_CLIENT_THREADS = 10;

  private boolean isRunning = false;

  public Server() {
    start();
  }

  public static void main(String[] args) {
    new Server();
  }

  private void start() {
    try {
      System.out.println("starting chat server");
      this.serverSocket = new ServerSocket(1200);
      System.out.println("chat server started");
      new Connection().start();
      semaphore = new Semaphore(10);
      isRunning = true;
    } catch (IOException e) {
      System.out.println("unable to start chat.server.");
      e.printStackTrace();
    }
  }

  protected class Connection extends Thread {

    private String user;
    private BufferedReader input;
    private Socket clientSocket;

    @Override
    public void run() {
      try {
        this.clientSocket = serverSocket.accept();
        System.out.println("client connected");
        input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        new Connection().start();

        receiveMessage();
      } catch (IOException e) {
        System.out.println("error while sending answer");
      }
    }

    private void receiveMessage() {
      while (isRunning) {
        try {
          final String clientInput = input.readLine();
          if (Command.EXIT.name().equalsIgnoreCase(clientInput)) {
            isRunning = false;
            System.out.println("received " + Command.EXIT.name() + " command");
            return;
          }
          final String[] requestInfo = clientInput.split("#", 2);
          String command = requestInfo[0];
          String input = requestInfo[1];
          processRequest(command, input);
        } catch (IOException e) {
          System.out.println("error while receiving message");
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }

    private void processRequest(String requiredCommand, String input) throws InterruptedException, IOException {
      System.out.println("received " + requiredCommand + " command");
      Command command = Command.valueOf(requiredCommand.toUpperCase());
      switch (command) {
        case GET -> {
          semaphore.acquire(1);
          final int amountOfMessages = messageHistory.size();
          try {
            int requiredAmountOfMessages = Integer.parseInt(input);
            sendMessagesToClient(amountOfMessages, requiredAmountOfMessages);
          } catch (NumberFormatException e) {
            sendMessagesToClient(amountOfMessages, DEFAULT_AMOUNT_OF_MESSAGES);
          }
          semaphore.release(1);
        }
        case SEND -> {
          semaphore.acquire(MAX_CLIENT_THREADS);
          messageHistory.add(new Message(user, input));
          semaphore.release(MAX_CLIENT_THREADS);
        }
        case REGISTER -> user = input;
        default -> throw new IllegalArgumentException("unknown RequestType#" + requiredCommand);
      }
    }

    private void sendMessagesToClient(int amountOfMessages, int requiredAmountOfMessages) throws IOException {
      final List<Message> messages = new ArrayList<>(
          messageHistory.subList(Math.max(amountOfMessages - requiredAmountOfMessages, 0), amountOfMessages));
      ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
      outputStream.writeObject(messages);
      outputStream.flush();
    }
  }
}
