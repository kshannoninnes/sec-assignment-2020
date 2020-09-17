package Interfaces;

import Models.Position;

import java.util.List;

@FunctionalInterface
public interface FilterPositions
{
    List<Position> filter(List<Position> allPositions);
}
