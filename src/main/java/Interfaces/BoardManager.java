package Interfaces;

import Models.MovableEntity;
import Models.Position;

import java.util.List;

public interface BoardManager
{
    void addEntity(MovableEntity entity);
    MovableEntity findEntity(Position position);
    void moveEntity(MovableEntity entity, Position nextPos);
    void removeEntity(MovableEntity entity);

    List<Position> filterPositions(List<Position> positions);
}
