package core.handlers;

import interfaces.*;

import models.Entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class ThreadHandler implements ThreadManager
{
    private final AttackManager attackHandler;
    private final ScheduledExecutorService attackExecutor;

    private final ScoreManager scoreHandler;
    private final ScheduledExecutorService scoreExecutor;

    private final SpawnManager spawnHandler;
    private final ScheduledExecutorService spawnExecutor;

    private final MovementManagerFactory moverFactory;
    private final Map<String, Future<?>> moveFutures;
    private final ScheduledExecutorService moveExecutor;

    private final ExecutorService selfExecutor;

    public ThreadHandler(ScoreManager scoreHandler, AttackManager attackHandler, SpawnManager spawnHandler,
                         MovementManagerFactory moverFactory)
    {
        this.scoreHandler = scoreHandler;
        this.scoreExecutor = Executors.newSingleThreadScheduledExecutor();

        this.attackHandler = attackHandler;
        this.attackExecutor = Executors.newSingleThreadScheduledExecutor();

        this.spawnHandler = spawnHandler;
        this.spawnExecutor = Executors.newSingleThreadScheduledExecutor();

        this.moverFactory = moverFactory;
        this.moveFutures = Collections.synchronizedMap(new HashMap<>());
        this.moveExecutor = Executors.newScheduledThreadPool(20);

        this.selfExecutor = Executors.newSingleThreadExecutor();
    }

    public void start()
    {
        // ThreadHandler looks after its own thread, rather than being a Runnable itself
        Runnable scheduleTask = () ->
        {
            createScoreScheduler();
            createAttackScheduler();
            createSpawnScheduler();
        };

        selfExecutor.execute(scheduleTask);
    }

    public void stop()
    {
        try
        {
            scoreExecutor.shutdown();
            attackExecutor.shutdown();
            spawnExecutor.shutdown();
            moveExecutor.shutdown();
            moveExecutor.awaitTermination(3000, TimeUnit.MILLISECONDS);
            selfExecutor.shutdown();

        } catch (InterruptedException e)
        {
            System.out.println("Shutdown was interrupted!\n");
            e.printStackTrace();
        }
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
            Entity entity = spawnHandler.spawnEntity();
            if(entity == null) return;

            createMoveScheduler(entity);
        };

        spawnExecutor.scheduleWithFixedDelay(spawnTask, 0, 2000, TimeUnit.MILLISECONDS);
    }

    private void createMoveScheduler(Entity entity)
    {
        MovementManager entityMover = moverFactory.createMover(entity);
        long delay = entity.getDelayInMillis();

        ScheduledFuture<?> future = moveExecutor.scheduleWithFixedDelay(entityMover::move, delay, delay, TimeUnit.MILLISECONDS);
        moveFutures.put(String.valueOf(entity.getId()), future);
    }
}
