package Core;

import Interfaces.*;
import Models.Entity;
import Models.Position;
import javafx.application.Platform;

import java.math.BigDecimal;
import java.util.*;

public class Game
{
    private final int gridHeight;
    private final int gridWidth;
    private final UserInterface gameUserInterface;
    private final List<Entity> activeEntities;


    // TODO Implement scoring, clean up EntityBuilder constructor, and clean up UserInterface interface
    // TODO Remove any game-specific stuff from JFXArena (should only be responsible for drawing stuff)
    // TODO Remove console printing throughout code
    public Game(int gridHeight, int gridWidth, UserInterface gameUserInterface)
    {
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
        this.gameUserInterface = gameUserInterface;
        this.activeEntities = Collections.synchronizedList(new LinkedList<>());
    }

    public void shutdown()
    {
        gameUserInterface.renderEntities(new LinkedList<>());
        System.out.println("Shutting down...");
    }

    /**
     * Entity Manipulation
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
        // removeIf iterates over the list behind the scenes
        synchronized (activeEntities)
        {
            activeEntities.removeIf(e -> e.getId() == entity.getId());
        }

        activeEntities.add(entity); // synchronizedList
        updateBoard();
    }

    public void removeEntity(Entity entity)
    {
        activeEntities.remove(entity); // synchronizedList
        updateBoard();
    }

    /**
     * Misc Helpers
     */

    public List<Position> filterPositions(List<Position> proposedPositions)
    {
        List<Position> acceptedPositions = new LinkedList<>(proposedPositions);
        for (Position p : proposedPositions)
        {
            synchronized (activeEntities)
            {
                for (Entity activeEntity : activeEntities)
                    if (activeEntity.getPosition().equals(p))
                        acceptedPositions.remove(p);
            }

            if (p.getX().compareTo(new BigDecimal(gridWidth)) >= 0 || p.getX().compareTo(BigDecimal.ZERO) < 0)
                acceptedPositions.remove(p);

            if (p.getY().compareTo(new BigDecimal(gridHeight)) >= 0 || p.getY().compareTo(BigDecimal.ZERO) < 0)
                acceptedPositions.remove(p);
        }

        return Collections.unmodifiableList(acceptedPositions);
    }

    private void updateBoard()
    {
        Platform.runLater(() -> gameUserInterface.renderEntities(new ArrayList<>(activeEntities)));
    }
}
