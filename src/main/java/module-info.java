module ch.teko.chatclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens ch.teko.chatclient to javafx.fxml;
    exports ch.teko.chatclient;
    exports ch.teko.chatclient.server;
    opens ch.teko.chatclient.server to javafx.fxml;
}