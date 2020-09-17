package Core;

import Interfaces.ArenaListener;
import Interfaces.FindEntity;
import Interfaces.Log;
import Interfaces.RemoveEntity;
import Models.Entity;
import Models.FireCommand;
import Models.Position;

import java.math.BigDecimal;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AttackHandler implements Runnable, ArenaListener
{
    private final Log logger;
    private final FindEntity finder;
    private final RemoveEntity remover;
    private final BlockingQueue<FireCommand> fireCommandQueue;

    public AttackHandler(Log logger, FindEntity finder, RemoveEntity remover)
    {
        fireCommandQueue = new ArrayBlockingQueue<>(10);
        this.logger = logger;
        this.finder = finder;
        this.remover = remover;
    }

    @Override
    public void run()
    {
        try
        {
            FireCommand fireCommand = fireCommandQueue.poll();
            if (fireCommand != null)
            {
                Entity attackedEntity = finder.find(fireCommand.getAttackLocation());
                int x = fireCommand.getAttackLocation().getX().intValue();
                int y = fireCommand.getAttackLocation().getY().intValue();
                String message;

                if(attackedEntity != null)
                {
                    remover.remove(attackedEntity);
                    message = String.format("Attack on [%d, %d] hits entity #%d.", x, y, attackedEntity.getId());
                }
                else
                {
                    message = String.format("Attack on %d, %d missed.", x, y);
                }

                logger.log(message);
                Thread.sleep(1000);
            }
        }
        catch (InterruptedException e) { /* Shutting down... */ }
    }

    public void squareClicked(int x, int y)
    {
        long timeInitiated = System.currentTimeMillis();
        Position attackLocation = new Position(new BigDecimal(x), new BigDecimal(y));
        FireCommand f = new FireCommand(timeInitiated, attackLocation);
        fireCommandQueue.offer(f);
    }
}
