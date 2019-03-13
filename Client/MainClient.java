import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Created by maxencebernier on 17/05/2017.
 */
public class Main extends Application {

    public void start(Stage primaryStage) throws Exception{
        new TFTPApp(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
