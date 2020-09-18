package Interfaces;

import Models.MovableEntity;
import Models.Position;

@FunctionalInterface
public interface FindEntity
{
    MovableEntity find(Position location);
}
