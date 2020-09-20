package core.handlers;

import interfaces.AttackManager;
import interfaces.BoardManager;
import interfaces.LogManager;
import interfaces.ScoreManager;

import models.Entity;
import models.FireCommand;
import models.Position;

import java.math.BigDecimal;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AttackHandler implements AttackManager
{
    private final BlockingQueue<FireCommand> fireCommandQueue;
    private final LogManager logHandler;
    private final BoardManager board;
    private final ScoreManager scoreboard;

    public AttackHandler(LogManager logHandler, BoardManager board, ScoreManager scoreboard)
    {
        this.logHandler = logHandler;
        this.board = board;
        this.scoreboard = scoreboard;
        fireCommandQueue = new ArrayBlockingQueue<>(10);
    }

    public Entity handleAttack()
    {
        try
        {
            FireCommand fireCommand = getFireCommand();
            if (fireCommand != null)
            {
                int x = fireCommand.getAttackLocation().getX().intValue();
                int y = fireCommand.getAttackLocation().getY().intValue();
                Entity destroyedEntity = board.findEntity(fireCommand.getAttackLocation());

                String message;
                if (destroyedEntity != null)
                {
                    int scoreGained = scoreboard.enemyKilled(destroyedEntity, fireCommand);
                    board.removeEntity(destroyedEntity);

                    message = String.format("Attack on [%d, %d] hits entity #%d for a bonus score of %d.", x, y, destroyedEntity.getId(), scoreGained);
                } else
                {
                    message = String.format("Attack on %d, %d missed.", x, y);
                }

                logHandler.log(message);
                Thread.sleep(1000);

                return destroyedEntity;
            }
        }
        catch (InterruptedException e) { /* ... */ }

        return null;
    }

    public void squareClicked(int x, int y)
    {
        long timeInitiated = System.currentTimeMillis();
        Position attackLocation = new Position(new BigDecimal(x), new BigDecimal(y));
        FireCommand f = new FireCommand(timeInitiated, attackLocation);

        fireCommandQueue.offer(f);
    }

    private FireCommand getFireCommand()
    {
        return fireCommandQueue.poll();
    }
}
