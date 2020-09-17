package Interfaces;

import Models.Entity;
import Models.Position;

@FunctionalInterface
public interface FindEntity
{
    Entity find(Position location);
}
