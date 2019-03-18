package src;

import View.ErrorView;
import View.FileQueueView;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class MailApp {


    Client client = new Client();
    List<Message> messages;
    int nbNewMessages = 0;
    
    public MailApp(Stage stage) {




        stage.setTitle("POP3 Client");
        stage.setWidth(600);
        stage.setHeight(400);




        Group root = new Group();
        String username = "username";
        String ip = "localhost";
        int port = 69;

        BorderPane container = new BorderPane();
        VBox vBox = new VBox();

        // zone de changement de port et ip du serveur distant
        HBox hBox1 = new HBox();
        Label label_user = new Label(" Username : ");
        hBox1.getChildren().add(label_user);
        TextField input_user = new TextField(username);
        hBox1.getChildren().add(input_user);

        HBox hBox2 = new HBox();
        Label label_password = new Label(" Password :  ");
        hBox2.getChildren().add(label_password);
        PasswordField input_password = new PasswordField();
        hBox2.getChildren().add(input_password);

        HBox hBox3 = new HBox();
        Label label_ip = new Label(" Server IP :   ");
        hBox3.getChildren().add(label_ip);
        TextField input_ip = new TextField(ip);
        hBox3.getChildren().add(input_ip);
        Label label_port = new Label("  Server Port : ");
        hBox3.getChildren().add(label_port);
        TextField input_port = new TextField(Integer.toString(port));
        hBox3.getChildren().add(input_port);

        HBox hBox4 = new HBox();
        Button button_connexion = new Button("Connexion");
        button_connexion.setOnAction(e -> {

        });
        Label label_spacing =new Label("                    ");
        hBox4.getChildren().add(label_spacing);
        hBox4.getChildren().add(button_connexion);
        vBox.getChildren().add(hBox1);
        vBox.getChildren().add(hBox2);
        vBox.getChildren().add(hBox3);
        vBox.getChildren().add(hBox4);



        container.setTop(vBox);







        root.getChildren().add(container);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        container.setPrefHeight(scene.getHeight());
        container.setPrefWidth(scene.getWidth());

        ErrorView errorView = new ErrorView(stage);
        client.addObserver(errorView);
    }




    public void connect() throws IOException {

        assert !client.isConnected() : "Client is already connnected";

        //checkChampsRemplis, erreur sinon

        String host = "A RELIER AU CHAMP HOST";
        int ip = Integer.parseInt("A RELIER AU CHAMP IP");
        String username = "A RELIER AU CHAMP HOST";
        String password = "A RELIER";

        if (client.connectToHost(host, ip)){

            if( client.sendAPOP(username, password)){

                //PASSER AU STAGE 2;
            }else{
                //errorMessage("Authentification failed.");
            }
        }else{
            client.disconnect();
            //errorMessage("Can not reach server. Verify ip and port are correctly configured.")
        }
    }

    public void quit()throws IOException{

        //Fonctions en plus
        disconnect();
    }

    public void disconnect()throws IOException{

        client.logout();
        client.disconnect();
    }

    public void refreshMessages() throws  IOException{

        nbNewMessages = client.getNumberOfNewMessages();

        messages = client.getMessages();
    }
}

