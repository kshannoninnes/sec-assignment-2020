package core;

import interfaces.BoardManager;
import interfaces.MovementManager;
import interfaces.MovementManagerFactory;

import models.Entity;

import core.handlers.MovementHandler;

public class MovementHandlerFactory implements MovementManagerFactory
{
    private final BoardManager board;

    public MovementHandlerFactory(BoardManager board)
    {
        this.board = board;
    }

    @Override
    public MovementManager createMover(Entity entity)
    {
        return new MovementHandler(entity, board);
    }
}
