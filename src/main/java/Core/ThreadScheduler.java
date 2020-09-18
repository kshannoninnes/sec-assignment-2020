package Core;

import Interfaces.*;

import Models.Entity;
import Models.MovableEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class ThreadScheduler implements Runnable
{
    private final GameManager boardHandler;

    private final AttackManager attackHandler;
    private final ScheduledExecutorService attackExecutor;

    private final ScoreManager scoreHandler;
    private final ScheduledExecutorService scoreExecutor;

    private final SpawnManager spawnHandler;
    private final ScheduledExecutorService spawnExecutor;

    private final MovementManagerFactory moverFactory;
    private final Map<String, Future<?>> moveFutures;
    private final ScheduledExecutorService moveExecutor;

    public ThreadScheduler(GameManager boardHandler, AttackManager attackHandler,
                           SpawnManager spawnHandler, ScoreManager scoreHandler,
                           MovementManagerFactory moverFactory)
    {
        this.boardHandler = boardHandler;

        this.attackHandler = attackHandler;
        this.attackExecutor = Executors.newSingleThreadScheduledExecutor();

        this.scoreHandler = scoreHandler;
        this.scoreExecutor = Executors.newSingleThreadScheduledExecutor();

        this.spawnHandler = spawnHandler;
        this.spawnExecutor = Executors.newSingleThreadScheduledExecutor();

        this.moverFactory = moverFactory;
        this.moveFutures = Collections.synchronizedMap(new HashMap<>());
        this.moveExecutor = Executors.newScheduledThreadPool(20);
    }

    @Override
    public void run()
    {
        createScoreScheduler();
        createAttackScheduler();
        createSpawnScheduler();
    }

    public void stop()
    {
        scoreExecutor.shutdownNow();
        attackExecutor.shutdownNow();
        spawnExecutor.shutdownNow();
        moveExecutor.shutdownNow();
        boardHandler.shutdown();
    }

    private void createScoreScheduler()
    {
        scoreExecutor.scheduleWithFixedDelay(scoreHandler::incrementScore, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private void createAttackScheduler()
    {
        Runnable attackTask = () ->
        {
            Entity destroyedEntity = attackHandler.handleAttack();
            if(destroyedEntity != null)
            {
                Future<?> future = moveFutures.remove(String.valueOf(destroyedEntity.getId()));
                if(future != null) future.cancel(true);
            }
        };

        attackExecutor.scheduleWithFixedDelay(attackTask, 0, 16, TimeUnit.MILLISECONDS);
    }

    private void createSpawnScheduler()
    {
        Runnable spawnTask = () ->
        {
            MovableEntity entity = spawnHandler.spawnEntity();
            if(entity == null) return;

            createMoveScheduler(entity);
        };

        spawnExecutor.scheduleAtFixedRate(spawnTask, 0, 2000, TimeUnit.MILLISECONDS);
    }

    private void createMoveScheduler(MovableEntity entity)
    {
        MovementManager entityMover = moverFactory.createMover(entity);
        long delay = entity.getDelayInMillis();

        ScheduledFuture<?> future = moveExecutor.scheduleAtFixedRate(entityMover::move, delay, delay, TimeUnit.MILLISECONDS);
        moveFutures.put(String.valueOf(entity.getId()), future);
    }
}
