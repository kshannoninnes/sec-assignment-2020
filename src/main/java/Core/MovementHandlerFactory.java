package Core;

import Interfaces.BoardManager;
import Interfaces.MovementManager;
import Interfaces.MovementManagerFactory;

import Models.MovableEntity;

import Core.Handlers.MovementHandler;

public class MovementHandlerFactory implements MovementManagerFactory
{
    private final BoardManager board;

    public MovementHandlerFactory(BoardManager board)
    {
        this.board = board;
    }

    @Override
    public MovementManager createMover(MovableEntity entity)
    {
        return new MovementHandler(entity, board);
    }
}
