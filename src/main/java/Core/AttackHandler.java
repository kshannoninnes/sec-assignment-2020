package Core;

import Interfaces.ArenaListener;
import Interfaces.FindEntity;
import Interfaces.RemoveEntity;
import Models.Entity;
import Models.FireCommand;
import Models.Position;

import java.math.BigDecimal;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AttackHandler implements Runnable, ArenaListener
{
    private final FindEntity finder;
    private final RemoveEntity remover;
    private final BlockingQueue<FireCommand> fireCommandQueue;

    public AttackHandler(FindEntity finder, RemoveEntity remover)
    {
        fireCommandQueue = new ArrayBlockingQueue<>(10);
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
                System.out.printf("Firing on %f, %f\n", fireCommand.getAttackLocation().getX(), fireCommand.getAttackLocation().getY());
                Entity attackedEntity = finder.find(fireCommand.getAttackLocation());
                if(attackedEntity != null)
                {
                    remover.remove(attackedEntity);
                    System.out.printf("Direct hit! Entity #%d destroyed!\n", attackedEntity.getId());
                }
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
        boolean success = fireCommandQueue.offer(f);
        if(success) System.out.println("Fire command queued");
        else System.out.println("Fire command failed to queue");
    }
}
