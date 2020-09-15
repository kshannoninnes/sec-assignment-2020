package Core;

import Interfaces.Game;
import Interfaces.Mover;
import Models.Entity;
import Models.Position;
import javafx.application.Platform;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

public class EntityMover implements Runnable
{
    private Entity entity;
    private Mover mover;

    public EntityMover(Entity entity, Mover mover)
    {
        this.entity = entity;
        this.mover = mover;
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
        catch (InterruptedException e)
        {
            System.out.printf("Removing enemy #%d from game...\n", entity.getId());
        }
        catch (ExecutionException e)
        {
            System.out.printf("Exception: %s", e.getCause());
        }
    }

    private List<Position> getPossibleMoves(Position currentPosition) throws ExecutionException, InterruptedException
    {
        List<Position> possibleMoves = new ArrayList<>(4);
        BigDecimal currX = currentPosition.getX();
        BigDecimal currY = currentPosition.getY();
        possibleMoves.add(new Position(currX.add(BigDecimal.ONE), currY));
        possibleMoves.add(new Position(currX.subtract(BigDecimal.ONE), currY));
        possibleMoves.add(new Position(currX, currY.add(BigDecimal.ONE)));
        possibleMoves.add(new Position(currX, currY.subtract(BigDecimal.ONE)));

        CompletableFuture<List<Position>> allowableMovesFuture = new CompletableFuture<>();
        Platform.runLater(() -> allowableMovesFuture.complete(mover.filterPositions(possibleMoves)));

        return allowableMovesFuture.get();
    }

    private void moveEntity(Position previousPos, List<Position> allowedMoves) throws InterruptedException
    {
        // The number to increment the current position by is equal to the difference between
        // the previousPos and the finalPos / 10, rounded to 1 decimal place.
        Position finalPos = allowedMoves.get(ThreadLocalRandom.current().nextInt(allowedMoves.size()));
        BigDecimal xInc = (finalPos.getX().subtract(previousPos.getX()))
                .divide(BigDecimal.TEN, 1, RoundingMode.HALF_EVEN);
        BigDecimal yInc = (finalPos.getY().subtract(previousPos.getY()))
                .divide(BigDecimal.TEN, 1, RoundingMode.HALF_EVEN);

        for(int ii = 0; ii < 10; ii++)
        {
            Position currentPos = entity.getPosition();

            // Because Entity is immutable, in order to "move" an entity, the existing
            // entity is replaced with a new one that has a slightly different position
            Position nextPos = new Position(currentPos.getX().add(xInc), currentPos.getY().add(yInc));
            entity = new Entity(entity.getId(), entity.getDelayInMillis(), entity.getImage(), nextPos);
            Platform.runLater(() -> mover.move(entity));

            Thread.sleep(50);
        }
    }
}
