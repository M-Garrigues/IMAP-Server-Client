import javafx.application.Application;
import javafx.stage.Stage;
import src.MailApp;

/**
 * Created by mathieu garrigues on 17/05/2017.
 */
public class MainClient extends Application {

    public void start(Stage primaryStage) throws Exception{
        new MailApp(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
