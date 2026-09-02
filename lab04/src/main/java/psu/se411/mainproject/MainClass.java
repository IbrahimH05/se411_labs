package psu.se411.mainproject;

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
            Label title = new Label("SE411 — Software Construction");
            Label subtitle = new Label("Maven + JavaFX project skeleton");

            VBox root = new VBox(10, title, subtitle);
            root.setAlignment(Pos.CENTER);

            primaryStage.setTitle("My Project");
            primaryStage.setScene(new Scene(root, 420, 180));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
