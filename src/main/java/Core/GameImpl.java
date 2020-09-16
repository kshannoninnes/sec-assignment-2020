package Core;

import Interfaces.*;
import Models.Entity;
import Models.Position;
import javafx.application.Platform;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

public class GameImpl implements Runnable
{
    private final int gridHeight;
    private final int gridWidth;
    private final UserInterface gameUserInterface;
    private final List<Entity> activeEntities;
    private final List<Position> allSpawnPositions;

    // For cancelling a specific mover when an entity is removed
    private final Map<String, Future<?>> moverFutures;

    // ThreadPools
    private final ScheduledExecutorService spawnerExec;
    private final ScheduledExecutorService moverExec;
    private final ScheduledExecutorService attackExec;

    // TODO Implement logging, scoring, and clean up UserInterface interface
    // TODO Remove any game-specific stuff from JFXArena (should only be responsible for drawing stuff)
    public GameImpl(int gridHeight, int gridWidth, UserInterface gameUserInterface, List<Position> allSpawnPositions)
    {
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
        this.gameUserInterface = gameUserInterface;
        this.activeEntities = Collections.synchronizedList(new LinkedList<>());
        this.allSpawnPositions = Collections.synchronizedList(allSpawnPositions);

        this.moverFutures = new HashMap<>();

        this.spawnerExec = Executors.newSingleThreadScheduledExecutor();
        this.moverExec = Executors.newScheduledThreadPool(10);
        this.attackExec = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void run()
    {
        AttackHandler attackHandler = new AttackHandler(this::findEntity, this::removeEntity);
        attackExec.scheduleWithFixedDelay(attackHandler, 0, 100, TimeUnit.MILLISECONDS);
        gameUserInterface.addSquareClickedListener(attackHandler);

        EntityBuilder builder = new EntityBuilder(2000, 1, this::addEntity, this::filterPositions, allSpawnPositions);
        spawnerExec.scheduleAtFixedRate(builder, 0, 2000, TimeUnit.MILLISECONDS);
    }

    public void shutdown()
    {
        spawnerExec.shutdownNow();
        moverExec.shutdownNow();
        attackExec.shutdownNow();
        System.out.println("Shutting down...");
    }

    /**
     * Entity Manipulation
     */

    private void addEntity(Entity entity)
    {
        EntityMover entityMover = new EntityMover(this::moveEntity, this::filterPositions, entity);
        ScheduledFuture<?> future = moverExec.scheduleAtFixedRate(entityMover, entity.getDelayInMillis(), entity.getDelayInMillis(), TimeUnit.MILLISECONDS);
        moverFutures.put(String.valueOf(entity.getId()), future);
        activeEntities.add(entity); // synchronizedList
        updateBoard();
    }

    private Entity findEntity(Position position)
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

        activeEntities.add(entity);
        updateBoard();
    }

    public void removeEntity(Entity entity)
    {
        Future<?> future = moverFutures.remove(String.valueOf(entity.getId()));
        if(future != null) future.cancel(true);
        activeEntities.remove(entity); // synchronizedList
        updateBoard();
    }

    public List<Position> filterPositions(List<Position> proposedPositions)
    {
        List<Position> acceptedPositions = new LinkedList<>(proposedPositions);


        for (Position p : proposedPositions)
        {
            synchronized (activeEntities)
            {
                for (Entity activeEntity : activeEntities)
                {
                    if (activeEntity.getPosition().equals(p))
                        acceptedPositions.remove(p);
                }
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
