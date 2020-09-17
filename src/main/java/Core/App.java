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

    private ThreadScheduler threadScheduler;
    private ExecutorService gameThread;
    
    @Override
    public void start(Stage stage) 
    {
        final int gridHeight = 9;
        final int gridWidth = 9;
        stage.setTitle("Fortress Defence (SEC Assignment 2020)");
        JFXArena arena = new JFXArena(gridHeight, gridWidth);

        TextArea textArea = new TextArea();

        Position topLeft = new Position(BigDecimal.ZERO, BigDecimal.ZERO);
        Position bottomLeft = new Position(new BigDecimal(gridHeight - 1), BigDecimal.ZERO);
        Position topRight = new Position(BigDecimal.ZERO, new BigDecimal(gridWidth - 1));
        Position bottomRight = new Position(new BigDecimal(gridHeight - 1), new BigDecimal(gridWidth - 1));
        List<Position> spawns = List.of(topLeft, topRight, bottomLeft, bottomRight);

        ToolBar toolbar = new ToolBar();
        Button btn1 = new Button("Start");
        Button btn2 = new Button("Stop");
        Label scoreLabel = new Label("Score: ");
        Label scoreAmountLabel = new Label("0");
        toolbar.getItems().addAll(btn1, btn2, scoreLabel, scoreAmountLabel);
        
        btn1.setOnAction((event) ->
        {
            Logger logger = new Logger(textArea);
            Game game = new Game(gridHeight, gridWidth, arena);
            AttackHandler attackHandler = new AttackHandler();
            arena.addSquareClickedListener(attackHandler);
            EntityBuilder spawnHandler = new EntityBuilder(spawns, game::filterPositions);

            threadScheduler = new ThreadScheduler(logger, game, attackHandler, spawnHandler);

            gameThread = Executors.newSingleThreadExecutor();
            gameThread.execute(threadScheduler);
        });

        btn2.setOnAction((event) ->
        {
            arena.clearListeners();
            threadScheduler.stop();
            gameThread.shutdown();
        });

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
        threadScheduler.stop();
        gameThread.shutdown();
    }
}
