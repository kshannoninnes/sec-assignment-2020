package core.handlers;

import interfaces.LossChecker;
import models.Entity;
import models.Position;

public class SingleLosingPosition implements LossChecker
{
    private final Position winningPosition;

    public SingleLosingPosition(Position winningPosition)
    {
        this.winningPosition = winningPosition;
    }

    public boolean isGameOver(Entity entity)
    {
        return entity.getPosition().exactEquals(winningPosition);
    }
}
