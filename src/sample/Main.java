package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application{
    public void createinterface(Stage primaryStage)
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Controller controller = loader.getController();
        setController(controller);
        windowparams(primaryStage, root);
        runStage(primaryStage, controller);
    }
    public void runStage(Stage stg, Controller controller)
    {
        stg.show();
        stg.setOnCloseRequest(e -> {
            controller.sendShutdownSignal();
            Platform.exit();
            System.exit(0);
        });
    }
    public void windowparams(Stage stg, Parent root)
    {
        stg.setTitle("Snake");
        stg.setWidth(1400);
        stg.setHeight(640);
        stg.setResizable(false);
        stg.setScene(new Scene(root));
    }
    public void setController(Controller controller)
    {
        GraphicsContext gc = controller.canvas.getGraphicsContext2D();
        controller.gc = gc;
        controller.LoadGraphics();
        controller.ip = getParameters().getRaw().get(0);
    }
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        createinterface(primaryStage);
    }
    public static void main(String[] args)
    {
        launch(args);
    }
}
