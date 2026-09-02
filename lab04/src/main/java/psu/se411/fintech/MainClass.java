package psu.se411.fintech;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainClass extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Label title = new Label("FinTech");
            Label subtitle = new Label("SE411 Software Construction — Maven + JavaFX");

            VBox root = new VBox(10, title, subtitle);
            root.setAlignment(Pos.CENTER);

            primaryStage.setTitle("FinTech");
            primaryStage.setScene(new Scene(root, 420, 180));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
