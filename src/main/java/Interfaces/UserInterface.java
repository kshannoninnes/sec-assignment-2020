package Interfaces;

import Models.Entity;
import Models.Position;

import java.util.List;

public interface UserInterface
{
    void addSquareClickedListener(ArenaListener listener);
    void renderEntities(List<Entity> entities);
    void drawLine(Position source, Position destination);
}
