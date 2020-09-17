package Core;

import Models.Position;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application 
{
    public static void main(String[] args) 
    {
        launch();        
    }

    private Game game;
    private final ExecutorService gameThread = Executors.newSingleThreadExecutor();
    
    @Override
    public void start(Stage stage) 
    {
        final int gridHeight = 9;
        final int gridWidth = 9;
        stage.setTitle("Fortress Defence (SEC Assignment 2020)");
        JFXArena arena = new JFXArena(gridHeight, gridWidth);

        TextArea textArea = new TextArea();
        Logger logger = new Logger(textArea);

        Position topLeft = new Position(BigDecimal.ZERO, BigDecimal.ZERO);
        Position bottomLeft = new Position(new BigDecimal(gridHeight - 1), BigDecimal.ZERO);
        Position topRight = new Position(BigDecimal.ZERO, new BigDecimal(gridWidth - 1));
        Position bottomRight = new Position(new BigDecimal(gridHeight - 1), new BigDecimal(gridWidth - 1));
        List<Position> spawns = List.of(topLeft, topRight, bottomLeft, bottomRight);

        game = new Game(logger, gridHeight, gridWidth, arena, spawns);
        gameThread.execute(game);



        ToolBar toolbar = new ToolBar();
        Button btn1 = new Button("My Button 1");
        Button btn2 = new Button("My Button 2");
        Label label = new Label("Score: 999");
        toolbar.getItems().addAll(btn1, btn2, label);
        
        btn1.setOnAction((event) -> System.out.println("Button 1 pressed"));
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(arena, textArea);
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
        game.shutdown();
        gameThread.shutdown();
    }
}
