package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
public class Main extends Application{
    private Scene scene;
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = (Parent)loader.load();
        Controller controller = (Controller)loader.getController();
        Group root1 = new Group();
        GraphicsContext gc = controller.canvas.getGraphicsContext2D();
        primaryStage.setTitle("Snake");
        controller.drawShapes(gc);

        primaryStage.setScene(new Scene(root));
        primaryStage.setWidth(1400);
        primaryStage.setHeight(640);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

    }



    public static void main(String[] args) {
        launch(args);
    }
}
