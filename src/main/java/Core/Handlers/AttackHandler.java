package Core.Handlers;

import Interfaces.AttackManager;
import Interfaces.BoardManager;
import Interfaces.LogManager;
import Interfaces.ScoreManager;

import Models.Entity;
import Models.FireCommand;
import Models.MovableEntity;
import Models.Position;

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
                MovableEntity destroyedEntity = board.findEntity(fireCommand.getAttackLocation());
                int x = fireCommand.getAttackLocation().getX().intValue();
                int y = fireCommand.getAttackLocation().getY().intValue();

                String message;
                if (destroyedEntity != null)
                {
                    board.removeEntity(destroyedEntity);
                    int scoreGained = scoreboard.enemyKilled(destroyedEntity, fireCommand);
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
