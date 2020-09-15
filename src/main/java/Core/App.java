package Core;

import Interfaces.Game;
import Models.Position;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application 
{
    public static void main(String[] args) 
    {
        launch();        
    }

    private ExecutorService threadPool = Executors.newFixedThreadPool(10);
    
    @Override
    public void start(Stage stage) 
    {
        final int gridHeight = 9;
        final int gridWidth = 9;
        stage.setTitle("Fortress Defence (SEC Assignment 2020)");
        JFXArena arena = new JFXArena(gridHeight, gridWidth);

        Position topLeft = new Position(BigDecimal.ZERO, BigDecimal.ZERO);
        Position bottomLeft = new Position(new BigDecimal(gridHeight - 1), BigDecimal.ZERO);
        Position topRight = new Position(BigDecimal.ZERO, new BigDecimal(gridWidth - 1));
        Position bottomRight = new Position(new BigDecimal(gridHeight - 1), new BigDecimal(gridWidth - 1));

        GameImpl game = new GameImpl(gridHeight, gridWidth, arena, List.of(topLeft, topRight, bottomLeft, bottomRight));
        game.start();




        ToolBar toolbar = new ToolBar();
        Button btn1 = new Button("My Button 1");
        Button btn2 = new Button("My Button 2");
        Label label = new Label("Score: 999");
        toolbar.getItems().addAll(btn1, btn2, label);
        
        btn1.setOnAction((event) -> System.out.println("Button 1 pressed"));
                    
        TextArea logger = new TextArea();
        logger.appendText("Hello\n");
        logger.appendText("World\n");
        
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(arena, logger);
        arena.setMinWidth(300.0);
        
        BorderPane contentPane = new BorderPane();
        contentPane.setTop(toolbar);
        contentPane.setCenter(splitPane);
        
        Scene scene = new Scene(contentPane, 800, 800);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception
    {
        super.stop();
        threadPool.shutdownNow();
        System.exit(0);
    }
}
