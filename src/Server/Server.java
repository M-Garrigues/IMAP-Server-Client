package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server{
    private static final int PORT = 5442;

    public static void main(String[] args) throws IOException {
        List<ServerThread> serverThreads = new ArrayList<>();
        ServerSocket serverSocket = new ServerSocket(PORT);

        while(true){
            ServerThread serverThread = new ServerThread(serverSocket.accept());
            serverThread.start();
            serverThreads.add(serverThread);
        }

    }
}