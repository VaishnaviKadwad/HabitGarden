import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point of HabitGarden. Loads the FXML layout, applies the CSS
 * and shows the main window.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 960, 640);
        scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());

        stage.setTitle("HabitGarden - Grow Your Routine");
        stage.setMinWidth(820);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}