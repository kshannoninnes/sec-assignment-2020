package interfaces;

import models.Entity;
import models.FireCommand;

public interface ScoreManager
{
    void incrementScore();
    int enemyKilled(Entity entity, FireCommand fireCommand);
}
