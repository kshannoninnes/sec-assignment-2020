package Core;

import Interfaces.*;
import Models.Entity;
import Models.FireCommand;
import Models.Position;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

public class GameImpl implements Mover, Spawner, ArenaListener
{
    private final int gridHeight;
    private final int gridWidth;
    private final UserInterface gameUserInterface;
    private final List<Entity> activeEntities;
    private final List<Position> allSpawnPositions;

    private final Map<String, Future<?>> moverFutures;
    private final ScheduledExecutorService spawnerThreadPool;
    private final ScheduledExecutorService moversThreadPool;
    private final BlockingQueue<FireCommand> fireCommandQueue;

    // TODO Implement logging, scoring, and fix player firing to allow for queued attacks
    // TODO Remove any game-specific stuff from JFXArena (should only be responsible for drawing stuff)
    public GameImpl(int gridHeight, int gridWidth, UserInterface gameUserInterface, List<Position> allSpawnPositions)
    {
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
        this.gameUserInterface = gameUserInterface;
        this.activeEntities = Collections.synchronizedList(new LinkedList<>());
        this.allSpawnPositions = Collections.unmodifiableList(allSpawnPositions);

        this.moverFutures = new HashMap<>();
        this.spawnerThreadPool = Executors.newSingleThreadScheduledExecutor();
        this.moversThreadPool = Executors.newScheduledThreadPool(10);
        this.fireCommandQueue = new ArrayBlockingQueue<>(10);

        this.gameUserInterface.addSquareClickedListener(this);
    }

    public void start()
    {
        Runnable builder = new EntityBuilder(2000, 1, this, allSpawnPositions);
        spawnerThreadPool.scheduleAtFixedRate(builder, 0, 2000, TimeUnit.MILLISECONDS);
    }

    // TODO Make GameImpl run on a separate thread to JFXArena, then implement a blocking queue
    // TODO for JFXArena to call when a square is clicked
    @Override
    public void squareClicked(int x, int y)
    {
        System.out.println("Square clicked..");
        Position attackLocation = new Position(new BigDecimal(x), new BigDecimal(y));

        Iterator<Entity> iter = activeEntities.iterator();
        while(iter.hasNext())
        {
            Entity e = iter.next();
            if(e.getPosition().equals(attackLocation))
            {
                System.out.printf("[%d] Removing entity from game...\n", e.getId());
                Future<?> removedMover = moverFutures.remove(String.valueOf(e.getId()));
                removedMover.cancel(true);
                iter.remove();
            }
        }
        updateBoard();
    }

    /**
     * Interface methods
     */

    @Override
    public void spawn(Entity entity)
    {
        EntityMover mover = new EntityMover(entity, this);
        ScheduledFuture<?> future = moversThreadPool.scheduleAtFixedRate(mover, 0, entity.getDelayInMillis(), TimeUnit.MILLISECONDS);
        moverFutures.put(String.valueOf(entity.getId()), future);
        activeEntities.add(entity);
        updateBoard();
    }

    @Override
    public void move(Entity entity)
    {
        activeEntities.removeIf(e -> e.getId() == entity.getId());
        activeEntities.add(entity);
        updateBoard();
    }

    @Override
    public List<Position> filterPositions(List<Position> proposedPositions)
    {
        List<Position> acceptedPositions = new LinkedList<>(proposedPositions);

        for(Position p : proposedPositions)
        {
            synchronized (activeEntities)
            {
                for (Entity activeEntity : activeEntities)
                {
                    if (activeEntity.getPosition().equals(p)) acceptedPositions.remove(p);
                }
            }

            if(p.getX().compareTo(new BigDecimal(gridWidth)) >= 0 || p.getX().compareTo(BigDecimal.ZERO) < 0)
                acceptedPositions.remove(p);

            if(p.getY().compareTo(new BigDecimal(gridHeight)) >= 0 || p.getY().compareTo(BigDecimal.ZERO) < 0)
                acceptedPositions.remove(p);
        }

        return acceptedPositions;
    }

    private void updateBoard()
    {
        gameUserInterface.renderEntities(Collections.unmodifiableList(activeEntities));
    }
}
