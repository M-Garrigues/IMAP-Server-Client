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
<<<<<<< HEAD
            ServerThread serverThread = new ServerThread(serverSocket.accept());
            serverThread.start();
            serverThreads.add(serverThread);
=======
            //Si le dernier thread créé n'est pas dans l'état Ready, on en crée un nouveau dans cet état
            //(pour être en mesure d'accepter une nouvelle connexion entrante)
            if(serverThreads.isEmpty() || serverThreads.get(serverThreads.size()-1).getServerState() != StateEnum.READY){
                ServerThread serverThread = new ServerThread(serverSocket);
                serverThread.run();
                serverThreads.add(serverThread);
            }
>>>>>>> 1e57b81be0a25efbd239f0269b69fe6fdcb25439
        }

    }
}