package Interfaces;

import Models.Position;

import java.util.List;

public interface Game
{
    List<Position> filterPositions(List<Position> allPositions);
}
