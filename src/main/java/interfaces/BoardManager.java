package interfaces;

import models.Entity;
import models.Position;

import java.util.List;

public interface BoardManager
{
    void addEntity(Entity entity);
    Entity findEntity(Position position);
    void moveEntity(Entity entity);
    void removeEntity(Entity entity);

    List<Position> filterPositions(List<Position> positions);
}
