package Core;

import Models.Entity;
import Models.FireCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class ThreadScheduler implements Runnable
{
    private final AttackHandler attackHandler;
    private final ScheduledExecutorService attackExecutor;

    private final ScoreHandler scoreHandler;
    private final ScheduledExecutorService scoreExecutor;

    private final EntityCreator entityCreator;
    private final ScheduledExecutorService spawnExecutor;

    private final Map<String, Future<?>> moveFutures;
    private final ScheduledExecutorService moveExecutor;

    private final Game game;
    private final Logger logger;

    public ThreadScheduler(Logger logger, Game game, AttackHandler attackHandler, EntityCreator entityCreator, ScoreHandler scoreHandler)
    {
        this.logger = logger;
        this.game = game;

        this.attackHandler = attackHandler;
        this.attackExecutor = Executors.newSingleThreadScheduledExecutor();

        this.scoreHandler = scoreHandler;
        this.scoreExecutor = Executors.newSingleThreadScheduledExecutor();

        this.entityCreator = entityCreator;
        this.spawnExecutor = Executors.newSingleThreadScheduledExecutor();

        this.moveFutures = new HashMap<>();
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
        game.shutdown();
    }

    private void createScoreScheduler()
    {
        scoreExecutor.scheduleWithFixedDelay(scoreHandler::incrementScore, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private void createAttackScheduler()
    {
        Runnable attackTask = () ->
        {
            try
            {
                FireCommand fireCommand = attackHandler.getFireCommand();
                if (fireCommand != null)
                {
                    Entity attackedEntity = game.findEntity(fireCommand.getAttackLocation());
                    int x = fireCommand.getAttackLocation().getX().intValue();
                    int y = fireCommand.getAttackLocation().getY().intValue();

                    String message;
                    if (attackedEntity != null)
                    {
                        game.removeEntity(attackedEntity);
                        Future<?> future = moveFutures.remove(String.valueOf(attackedEntity.getId()));
                        if(future != null) future.cancel(true);
                        scoreHandler.enemyKilled(attackedEntity, fireCommand);
                        message = String.format("Attack on [%d, %d] hits entity #%d.", x, y, attackedEntity.getId());
                    } else
                    {
                        message = String.format("Attack on %d, %d missed.", x, y);
                    }

                    logger.log(message);
                    Thread.sleep(1000);
                }
            }
            catch (InterruptedException e) { /* ... */ }
        };

        attackExecutor.scheduleWithFixedDelay(attackTask, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void createSpawnScheduler()
    {
        Runnable spawnTask = () ->
        {
            try
            {
                CompletableFuture<Entity> spawnFuture = new CompletableFuture<>();
                spawnFuture.complete(entityCreator.getEntity());
                Entity newEntity = spawnFuture.get();

                if (newEntity == null) return;

                game.addEntity(newEntity);
                logger.log(String.format("Entity #%d spawned with delay %d.", newEntity.getId(), newEntity.getDelayInMillis()));
                createMoveScheduler(newEntity);
            }
            catch (InterruptedException | ExecutionException e) { /* ... */ }
        };

        spawnExecutor.scheduleAtFixedRate(spawnTask, 0, 2000, TimeUnit.MILLISECONDS);
    }

    private void createMoveScheduler(Entity entity)
    {
        EntityMover entityMover = new EntityMover(entity, game::filterPositions, game::moveEntity);
        long delay = entity.getDelayInMillis();
        ScheduledFuture<?> future = moveExecutor.scheduleAtFixedRate(entityMover, delay, delay, TimeUnit.MILLISECONDS);
        moveFutures.put(String.valueOf(entity.getId()), future);
    }
}
