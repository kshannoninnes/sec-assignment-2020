package core.handlers;

import interfaces.GameManager;
import interfaces.ThreadManager;
import interfaces.UserInterface;
import interfaces.LossManager;

import models.Entity;
import models.Position;

import javafx.application.Platform;
import java.math.BigDecimal;
import java.util.*;

public class GameHandler implements GameManager
{
    private final int gridHeight;
    private final int gridWidth;
    private final UserInterface ui;

    private final List<Entity> activeEntities;

    private final LossManager lossHandler;
    private ThreadManager threadHandler;


    public GameHandler(int gridHeight, int gridWidth, UserInterface ui, LossManager lossHandler)
    {
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
        this.ui = ui;

        this.activeEntities = Collections.synchronizedList(new LinkedList<>());

        this.lossHandler = lossHandler;
    }

    /*
     * GameManager methods
     */

    public void start()
    {
        if(threadHandler == null) throw new IllegalStateException("GameHandler needs a ThreadHandler!");
        threadHandler.start();
    }

    public void stop()
    {
        activeEntities.clear();
        updateBoard();
        endGame();
    }

    public void setThreadHandler(ThreadManager threadHandler)
    {
        this.threadHandler = threadHandler;
    }

    /*
     * BoardManager methods
     */

    public void addEntity(Entity entity)
    {
        activeEntities.add(entity); // synchronizedList
        updateBoard();
    }

    public Entity findEntity(Position position)
    {
        synchronized (activeEntities)
        {
            for(Entity e : activeEntities)
            {
                if(e.getPosition().equals(position)) return e;
            }
        }

        return null;
    }

    public void moveEntity(Entity entity)
    {
        boolean wasActive;

        // removeIf iterates over the list behind the scenes
        synchronized (activeEntities)
        {
            wasActive = activeEntities.removeIf(e -> e.getId() == entity.getId());
        }

        if(wasActive)
        {
            activeEntities.add(entity); // synchronizedList
            updateBoard();

            if(lossHandler.isGameOver(entity)) endGame();
        }
    }

    public void removeEntity(Entity entity)
    {
        activeEntities.remove(entity); // synchronizedList
        updateBoard();
    }

    /**
     * Return a list with any invalid positions (based on board state) removed
     */
    public List<Position> filterPositions(List<Position> proposedPositions)
    {
        List<Position> acceptedPositions = new LinkedList<>(proposedPositions);
        for (Position proposedPosition : proposedPositions)
        {
            synchronized (activeEntities)
            {
                for (Entity activeEntity : activeEntities)
                    if (activeEntity.getPosition().equals(proposedPosition))
                        acceptedPositions.remove(proposedPosition);
            }

            if (proposedPosition.getX().compareTo(new BigDecimal(gridWidth)) >= 0
                    || proposedPosition.getX().compareTo(BigDecimal.ZERO) < 0)
                acceptedPositions.remove(proposedPosition);

            if (proposedPosition.getY().compareTo(new BigDecimal(gridHeight)) >= 0
                    || proposedPosition.getY().compareTo(BigDecimal.ZERO) < 0)
                acceptedPositions.remove(proposedPosition);
        }

        return Collections.unmodifiableList(acceptedPositions);
    }

    /*
     * Private methods
     */

    private void updateBoard()
    {
        Platform.runLater(() -> ui.renderEntities(new ArrayList<>(activeEntities)));
    }

    private void endGame()
    {
        threadHandler.stop();
    }
}
