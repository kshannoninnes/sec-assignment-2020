package Core;

import Models.MovableEntity;
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
                    MovableEntity attackedMovableEntity = game.findEntity(fireCommand.getAttackLocation());
                    int x = fireCommand.getAttackLocation().getX().intValue();
                    int y = fireCommand.getAttackLocation().getY().intValue();

                    String message;
                    if (attackedMovableEntity != null)
                    {
                        game.removeEntity(attackedMovableEntity);
                        Future<?> future = moveFutures.remove(String.valueOf(attackedMovableEntity.getId()));
                        if(future != null) future.cancel(true);
                        int scoreGained = scoreHandler.enemyKilled(attackedMovableEntity, fireCommand);
                        message = String.format("Attack on [%d, %d] hits entity #%d for a bonus score of %d.", x, y, attackedMovableEntity.getId(), scoreGained);
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
                CompletableFuture<MovableEntity> spawnFuture = new CompletableFuture<>();
                spawnFuture.complete(entityCreator.getEntity());
                MovableEntity newMovableEntity = spawnFuture.get();

                if (newMovableEntity == null) return;

                game.addEntity(newMovableEntity);
                logger.log(String.format("Entity #%d spawned with delay %d.", newMovableEntity.getId(), newMovableEntity.getDelayInMillis()));
                createMoveScheduler(newMovableEntity);
            }
            catch (InterruptedException | ExecutionException e) { /* ... */ }
        };

        spawnExecutor.scheduleAtFixedRate(spawnTask, 0, 2000, TimeUnit.MILLISECONDS);
    }

    private void createMoveScheduler(MovableEntity movableEntity)
    {
        EntityMover entityMover = new EntityMover(movableEntity, game::filterPositions, game::moveEntity);
        long delay = movableEntity.getDelayInMillis();
        ScheduledFuture<?> future = moveExecutor.scheduleAtFixedRate(entityMover, delay, delay, TimeUnit.MILLISECONDS);
        moveFutures.put(String.valueOf(movableEntity.getId()), future);
    }
}
