package Core;

import Models.Entity;
import Models.FireCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class ThreadScheduler implements Runnable
{
    private final AttackHandler attackHandler;
    private final ScheduledExecutorService attackScheduler;

    private final EntityBuilder entityBuilder;
    private final ScheduledExecutorService spawnScheduler;

    private final Map<String, Future<?>> moverFutures;
    private final ScheduledExecutorService moverExec;

    private final Game game;
    private final Logger logger;

    public ThreadScheduler(Logger logger, Game game, AttackHandler attackHandler, EntityBuilder entityBuilder)
    {
        this.logger = logger;
        this.game = game;

        this.attackHandler = attackHandler;
        this.attackScheduler = Executors.newSingleThreadScheduledExecutor();

        this.entityBuilder = entityBuilder;
        this.spawnScheduler = Executors.newSingleThreadScheduledExecutor();

        this.moverFutures = new HashMap<>();
        this.moverExec = Executors.newScheduledThreadPool(20);
    }

    @Override
    public void run()
    {
        createAttackScheduler();
        createSpawnScheduler();
    }

    public void stop()
    {
        attackScheduler.shutdownNow();
        spawnScheduler.shutdownNow();
        moverExec.shutdownNow();
        game.shutdown();
    }

    private void createSpawnScheduler()
    {
        Runnable spawnTask = () ->
        {
            try
            {
                CompletableFuture<Entity> spawnFuture = new CompletableFuture<>();
                spawnFuture.complete(entityBuilder.getEntity());
                Entity newEntity = spawnFuture.get();

                if (newEntity == null) return;

                game.addEntity(newEntity);
                logger.log(String.format("Entity #%d spawned with delay %d.", newEntity.getId(), newEntity.getDelayInMillis()));
                createMoveScheduler(newEntity);
            }
            catch (InterruptedException | ExecutionException e) { /* ... */ }
        };

        spawnScheduler.scheduleAtFixedRate(spawnTask, 0, 2000, TimeUnit.MILLISECONDS);
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
                        Future<?> future = moverFutures.remove(String.valueOf(attackedEntity.getId()));
                        if(future != null) future.cancel(true);
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

        attackScheduler.scheduleWithFixedDelay(attackTask, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void createMoveScheduler(Entity entity)
    {
        EntityMover entityMover = new EntityMover(entity, game::filterPositions, game::moveEntity);
        long delay = entity.getDelayInMillis();
        ScheduledFuture<?> future = moverExec.scheduleAtFixedRate(entityMover, delay, delay, TimeUnit.MILLISECONDS);
        moverFutures.put(String.valueOf(entity.getId()), future);
    }
}
