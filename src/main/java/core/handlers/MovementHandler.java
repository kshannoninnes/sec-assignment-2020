package core.handlers;

import interfaces.BoardManager;
import interfaces.MovementManager;

import models.Entity;
import models.MovableEntity;
import models.Position;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MovementHandler implements MovementManager
{
    private Entity entity;

    private final BoardManager board;

    public MovementHandler(Entity entity, BoardManager board)
    {
        this.entity = entity;
        this.board = board;
    }

    public void move()
    {
        try
        {
            Position currentPos = entity.getPosition();
            Position previousPos = new Position(currentPos.getX(), currentPos.getY());
            List<Position> allowedMoves = getPossibleMoves(currentPos);
            if(allowedMoves.size() == 0) return;

            moveEntity(previousPos, allowedMoves);
        }
        catch (InterruptedException e)
        {
            System.out.println("Interrupted Mover!");
        }
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

        return board.filterPositions(Collections.unmodifiableList(possibleMoves));
    }

    private void moveEntity(Position previousPos, List<Position> allowedMoves) throws InterruptedException
    {
        final int NUM_FRAMES = 10;

        // The number to increment the current position by is equal to
        // (finalPos - originalPos) / NUM_FRAMES, rounded to 1 decimal place.
        // eg. Moving to 7,0 from 8,0 is a difference of -1,0
        Position finalPos = allowedMoves.get(ThreadLocalRandom.current().nextInt(allowedMoves.size()));
        BigDecimal xInc = (finalPos.getX().subtract(previousPos.getX()))
                .divide(new BigDecimal(NUM_FRAMES), 1, RoundingMode.HALF_EVEN);
        BigDecimal yInc = (finalPos.getY().subtract(previousPos.getY()))
                .divide(new BigDecimal(NUM_FRAMES), 1, RoundingMode.HALF_EVEN);

        for(int ii = 0; ii < NUM_FRAMES; ii++)
        {
            long start = System.currentTimeMillis();
            Position currentPos = entity.getPosition();
            Position nextPos = new Position(currentPos.getX().add(xInc), currentPos.getY().add(yInc));

            // Because Entity is immutable, in order to "move" an entity, the existing
            // entity is replaced with a new one that has a slightly different position
            entity = new MovableEntity(entity.getId(), entity.getImage(), nextPos, entity.getDelayInMillis());
            board.moveEntity(entity);
            long runTime = Math.abs(System.currentTimeMillis() - start);

            Thread.sleep(50 - runTime);
        }
    }
}
