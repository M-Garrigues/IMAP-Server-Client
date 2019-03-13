package src;

import View.ErrorView;
import View.FileQueueView;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.*;

public class MailApp {
    public MailApp(Stage stage) {
        stage.setTitle("TFTP_SendFile");
        stage.setWidth(600);
        stage.setHeight(400);




        Group root = new Group();
        String ip = "localhost";
        int port = 69;

        Client client = new Client(ip, port);
        BorderPane container = new BorderPane();
        VBox vBox = new VBox();

        // zone de changement de port et ip du serveur distant

        Label current = new Label("  Ip: " + ip + " et Port: " + port);
        HBox hBox = new HBox();
        Label label_ip = new Label(" Adresse: ");
        hBox.getChildren().add(label_ip);
        TextField input_ip = new TextField(ip);
        hBox.getChildren().add(input_ip);
        Label label_port = new Label("Port: ");
        hBox.getChildren().add(label_port);
        TextField input_port = new TextField(Integer.toString(port));
        hBox.getChildren().add(input_port);
        Button button_change = new Button("Appliquer");
        button_change.setOnAction(e -> {
            current.setText("Ip: " + input_ip.getText() + " et Port: " + input_port.getText());
            client.setIP(input_ip.getText());
            client.setPort(Integer.parseInt(input_port.getText()));
        });
        hBox.getChildren().add(button_change);
        vBox.getChildren().add(hBox);


        vBox.getChildren().add(current);
        container.setTop(vBox);



        Button button_chose = new Button("Choisir un fichier à envoyer");
        button_chose.setOnAction(e -> {
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showOpenDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {

                //client.sendFile(chooser.getSelectedFile().getPath().replace("\\", "/"));
            }
        });
        container.setLeft(button_chose);

        FileQueueView fileQueue = new FileQueueView();
        client.addObserver(fileQueue);
        container.setCenter(fileQueue);
        root.getChildren().add(container);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        container.setPrefHeight(scene.getHeight());
        container.setPrefWidth(scene.getWidth());

        ErrorView errorView = new ErrorView(stage);
        client.addObserver(errorView);
    }
}

