package Interfaces;

import Models.FireCommand;
import Models.MovableEntity;

public interface ScoreManager
{
    void incrementScore();
    int enemyKilled(MovableEntity entity, FireCommand fireCommand);
}
