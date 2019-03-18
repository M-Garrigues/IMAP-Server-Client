package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerThread extends Thread{
    //private static final List<String> users = new ArrayList<String>.

    private StateEnum serverState;
    private ServerSocket serverSocket;
    private Socket serverThreadSocket;
    private InputStreamReader inputStreamReader;
    private PrintWriter out;
    private BufferedReader in;

    private String currentUser;

    public ServerThread(ServerSocket serverSocket) throws IOException {
        this.serverState = StateEnum.STOPPED;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run(){
        serverState = StateEnum.READY;

        //Attente d'une connexion
        try {
            serverThreadSocket = serverSocket.accept();

            //Initialisation des canaux d'entrée et de sortie
            inputStreamReader = new InputStreamReader(serverThreadSocket.getInputStream());
            out = new PrintWriter(serverThreadSocket.getOutputStream(), true);
            in = new BufferedReader(inputStreamReader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Passage dans l'état Authorization
        serverState = StateEnum.AUTHORIZATION;
        out.println("+OK POP3 server ready");

        while(true){
            switch(serverState){
                case AUTHORIZATION:
                    handleAuthorizationState();
                    break;
            }
        }

    }

    private void handleAuthorizationState(){
        String input;
            try {
                while((input = in.readLine()) != null){
                    String[] params = input.split(" ", 2);
                    if(params[0].equals("USER")){
                        if(!params[1].equals("utilisateur1") && !params[1].equals("utilisateur2")){
                            out.println("-ERR unknown user : " + params[1]);
                        }
                        else{
                            currentUser = params[1];

                            out.println("+OK");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public StateEnum getServerState(){
        return serverState;
    }
}