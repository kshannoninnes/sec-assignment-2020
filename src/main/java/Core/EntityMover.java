package Core;

import Interfaces.MoveEntity;
import Interfaces.FilterPositions;
import Models.Entity;
import Models.Position;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EntityMover implements Runnable
{
    private final FilterPositions filter;
    private final MoveEntity mover;
    private Entity entity;

    public EntityMover(Entity entity, FilterPositions filter, MoveEntity mover)
    {
        this.entity = entity;
        this.mover = mover;
        this.filter = filter;
    }

    @Override
    public void run()
    {
        try
        {
            Position currentPos = entity.getPosition();
            Position previousPos = new Position(currentPos.getX(), currentPos.getY());
            List<Position> allowedMoves = getPossibleMoves(currentPos);
            if(allowedMoves.size() > 0)  moveEntity(previousPos, allowedMoves);
        }
        catch (InterruptedException e) { /* Shutting down... */ }
    }

    private List<Position> getPossibleMoves(Position currentPosition)
    {
        List<Position> possibleMoves = new ArrayList<>(4);
        BigDecimal currX = currentPosition.getX();
        BigDecimal currY = currentPosition.getY();
        possibleMoves.add(new Position(currX.add(BigDecimal.ONE), currY));
        possibleMoves.add(new Position(currX.subtract(BigDecimal.ONE), currY));
        possibleMoves.add(new Position(currX, currY.add(BigDecimal.ONE)));
        possibleMoves.add(new Position(currX, currY.subtract(BigDecimal.ONE)));

        return filter.filter(Collections.unmodifiableList(possibleMoves));
    }

    private void moveEntity(Position previousPos, List<Position> allowedMoves) throws InterruptedException
    {
        // The number to increment the current position by is equal to
        // (finalPos - originalPos) / 10, rounded to 1 decimal place.
        Position finalPos = allowedMoves.get(ThreadLocalRandom.current().nextInt(allowedMoves.size()));
        BigDecimal xInc = (finalPos.getX().subtract(previousPos.getX()))
                .divide(BigDecimal.TEN, 1, RoundingMode.HALF_EVEN);
        BigDecimal yInc = (finalPos.getY().subtract(previousPos.getY()))
                .divide(BigDecimal.TEN, 1, RoundingMode.HALF_EVEN);

        for(int ii = 0; ii < 10; ii++)
        {
            long start = System.currentTimeMillis();
            Position currentPos = entity.getPosition();

            // Because Entity is immutable, in order to "move" an entity, the existing
            // entity is replaced with a new one that has a slightly different position
            Position nextPos = new Position(currentPos.getX().add(xInc), currentPos.getY().add(yInc));
            entity = new Entity(entity.getId(), entity.getDelayInMillis(), entity.getImage(), nextPos);
            mover.move(entity);
            long runTime = Math.abs(System.currentTimeMillis() - start);

            Thread.sleep(50 - runTime);
        }
    }
}
