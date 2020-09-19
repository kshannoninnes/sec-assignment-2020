package interfaces;

import models.Entity;

public interface MovementManagerFactory
{
    MovementManager createMover(Entity entity);
}
