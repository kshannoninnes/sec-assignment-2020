package Core;

import Interfaces.*;
import Models.MovableEntity;
import Models.Position;
import javafx.application.Platform;

import java.math.BigDecimal;
import java.util.*;

public class Game
{
    private final int gridHeight;
    private final int gridWidth;
    private final UserInterface ui;
    private final List<MovableEntity> activeEntities;


    // TODO Clean up EntityBuilder constructor, change hard coupled classes to interfaces
    // TODO !!!!!!!! Make win condition !!!!!!!!
    // TODO Remove any game-specific stuff from JFXArena (should only be responsible for drawing stuff)
    // TODO Remove console printing throughout code
    public Game(int gridHeight, int gridWidth, UserInterface ui)
    {
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
        this.ui = ui;

        this.activeEntities = Collections.synchronizedList(new LinkedList<>());
    }

    public void shutdown()
    {
        activeEntities.clear();
        updateBoard();
    }

    /**
     * Entity Manipulation
     */

    public void addEntity(MovableEntity entity)
    {
        activeEntities.add(entity); // synchronizedList
        updateBoard();
    }

    public MovableEntity findEntity(Position position)
    {
        synchronized (activeEntities)
        {
            for(MovableEntity e : activeEntities)
            {
                if(e.getPosition().equals(position)) return e;
            }
        }

        return null;
    }

    public void moveEntity(MovableEntity entity)
    {
        // removeIf iterates over the list behind the scenes
        synchronized (activeEntities)
        {
            activeEntities.removeIf(e -> e.getId() == entity.getId());
        }

        activeEntities.add(entity); // synchronizedList
        updateBoard();
    }

    public void removeEntity(MovableEntity entity)
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
        for (Position proposedPosition : proposedPositions)
        {
            synchronized (activeEntities)
            {
                for (MovableEntity activeEntity : activeEntities)
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

    private void updateBoard()
    {
        Platform.runLater(() -> ui.renderEntities(new ArrayList<>(activeEntities)));
    }
}
