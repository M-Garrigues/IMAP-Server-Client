package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
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

    private String currentWelcomeMessage;
    private String currentUser;
    private int passwordErrors = 0;

    public ServerThread(ServerSocket serverSocket) throws IOException {
        this.serverState = StateEnum.STOPPED;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run(){
        serverState = StateEnum.READY;

        while(true){
            switch(serverState){
                case READY:
                    handleReadyState();
                    break;
                case AUTHORIZATION:
                    handleAuthorizationState();
                    break;
                case WAITING_PASSWORD:
                    handleWaitingPasswordState();
                    break;
                case TRANSACTION:
                    handleTransactionState();
                    break;
            }
        }

    }

    private void handleReadyState(){
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
        this.setCurrentWelcomeMessage();
        out.println("+OK POP3 server ready " + currentWelcomeMessage);
    }

    private void handleAuthorizationState(){
            try {
                String input = in.readLine();
                String[] params = input.split(" ", 2);
                if(params[0].equals("USER")){
                    if(!params[1].equals("user1") && !params[1].equals("user2")){
                        out.println("-ERR unknown user : " + params[1]);
                    }
                    else{
                        currentUser = params[1];
                        serverState = StateEnum.WAITING_PASSWORD;

                        out.println("+OK");
                    }
                }
                else if(params[0].equals("APOP")){
                    String[] apopParams = params[1].split(" ", 2);
                    if(!apopParams[0].equals("user1") && !apopParams[0].equals("user2")){
                        out.println("-ERR unknown user : " + params[1]);
                    }
                    else{
                        String rightHash = String.format("%02x", MessageDigest.getInstance("MD5")
                                .digest((currentWelcomeMessage + "1234").getBytes()));
                        if(!apopParams[1].equals(rightHash)){
                            passwordErrors++;

                            if(passwordErrors >= 3){
                                out.println("-ERR Invalid password. Too many errors, closing connection...");
                                serverThreadSocket.close();
                                serverState = StateEnum.READY;
                            }
                            else{
                                out.println("-ERR Invalid password.");
                                serverState = StateEnum.AUTHORIZATION;
                            }
                        }
                        else{
                            passwordErrors = 0;
                            currentUser = apopParams[0];
                            serverState = StateEnum.TRANSACTION;
                            out.println("+OK");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
    }

    private void handleWaitingPasswordState(){
        try {
            String input = in.readLine();
            String[] params = input.split(" ", 2);
            if(params[0].equals("PASS")){
                if(!params[1].equals("1234")){
                    passwordErrors++;

                    if(passwordErrors >= 3){
                        out.println("-ERR Invalid password. Too many errors, closing connection...");
                        serverThreadSocket.close();
                        serverState = StateEnum.READY;
                    }
                    else{
                        out.println("-ERR Invalid password.");
                        serverState = StateEnum.AUTHORIZATION;
                    }
                }
                else{
                    passwordErrors = 0;
                    currentUser = params[1];
                    serverState = StateEnum.TRANSACTION;
                    out.println("+OK");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleTransactionState(){
        //TODO
    }

    private void setCurrentWelcomeMessage(){
        long threadId = this.getId();
        long timestamp = new Timestamp(System.currentTimeMillis()).getTime();
        currentWelcomeMessage = "<" + threadId + "." + timestamp + "@bestpop3serverever.com>";
    }

    public StateEnum getServerState(){
        return serverState;
    }
}