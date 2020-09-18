package Interfaces;

import Models.MovableEntity;

public interface MovementManagerFactory
{
    MovementManager createMover(MovableEntity entity);
}
