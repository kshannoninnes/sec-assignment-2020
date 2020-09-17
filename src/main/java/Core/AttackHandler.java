package Core;

import Interfaces.ArenaListener;
import Models.FireCommand;
import Models.Position;

import java.math.BigDecimal;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AttackHandler implements ArenaListener
{
    private final BlockingQueue<FireCommand> fireCommandQueue;

    public AttackHandler()
    {
        fireCommandQueue = new ArrayBlockingQueue<>(10);
    }

    public FireCommand getFireCommand()
    {
        return fireCommandQueue.poll();
    }

    public void squareClicked(int x, int y)
    {
        long timeInitiated = System.currentTimeMillis();
        Position attackLocation = new Position(new BigDecimal(x), new BigDecimal(y));
        FireCommand f = new FireCommand(timeInitiated, attackLocation);

        fireCommandQueue.offer(f);
    }
}
