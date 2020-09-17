package Interfaces;

import Models.Entity;

import java.util.List;

public interface UserInterface
{
    void addSquareClickedListener(ArenaListener listener);
    void renderEntities(List<Entity> entities);
}
