package core;

import core.handlers.*;
import interfaces.*;
import models.Position;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

public class App extends Application 
{
    public static void main(String[] args) 
    {
        launch();        
    }

    private GameManager game;
    private boolean running = false;
    
    @Override
    public void start(Stage stage) 
    {
        final int gridHeight = 9;
        final int gridWidth = 9;
        stage.setTitle("Fortress Defence (SEC Assignment 2020)");

        TextArea textArea = new TextArea();
        ToolBar toolbar = new ToolBar();
        SplitPane splitPane = new SplitPane();
        BorderPane contentPane = new BorderPane();

        Button btn1 = new Button("Start");
        Button btn2 = new Button("Stop");
        Label scoreLabel = new Label("Score: ");
        Label scoreValueLabel = new Label("0");
        JFXArena arena = new JFXArena(gridHeight, gridWidth, scoreValueLabel);

        toolbar.getItems().addAll(btn1, btn2, scoreLabel, scoreValueLabel);
        splitPane.getItems().addAll(arena, textArea);
        arena.setMinWidth(300.0);
        contentPane.setTop(toolbar);
        contentPane.setCenter(splitPane);



        // Intentional integer division
        Position winningPosition = new Position(new BigDecimal(gridHeight / 2), new BigDecimal(gridWidth / 2));

        Position topLeft = new Position(BigDecimal.ZERO, BigDecimal.ZERO);
        Position bottomLeft = new Position(new BigDecimal(gridHeight - 1), BigDecimal.ZERO);
        Position topRight = new Position(BigDecimal.ZERO, new BigDecimal(gridWidth - 1));
        Position bottomRight = new Position(new BigDecimal(gridHeight - 1), new BigDecimal(gridWidth - 1));
        List<Position> spawns = List.of(topLeft, topRight, bottomLeft, bottomRight);
        
        btn1.setOnAction((event) ->
        {
            if(running) return;

            LossManager winChecker = new SingleLosingPosition(winningPosition);
            game = new GameHandler(gridHeight, gridWidth, arena, winChecker);

            LogManager logHandler = new LogHandler(textArea);
            ScoreManager scoreHandler = new ScoreHandler(arena);
            SpawnHandler spawnHandler = new SpawnHandler(logHandler, game, spawns);
            AttackManager attackHandler = new AttackHandler(logHandler, game, scoreHandler);
            MovementManagerFactory moverFactory = new MovementHandlerFactory(game);
            arena.addSquareClickedListener(attackHandler);

            ThreadHandler threadHandler = new ThreadHandler(scoreHandler, attackHandler, spawnHandler, moverFactory);
            game.setThreadHandler(threadHandler);

            game.start();
            running = true;
        });

        btn2.setOnAction((event) ->
        {
            if(!running) return;

            arena.clearListeners();
            game.stop();

            running = false;
        });
        
        Scene scene = new Scene(contentPane, 800, 800);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception
    {
        super.stop();
        game.stop();
    }
}
