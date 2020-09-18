package Interfaces;

import Models.MovableEntity;

@FunctionalInterface
public interface MoveEntity
{
    void move(MovableEntity movableEntity);
}
