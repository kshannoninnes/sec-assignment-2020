package core.handlers;

import interfaces.ScoreManager;
import interfaces.UserInterface;

import models.Entity;
import models.FireCommand;

public class ScoreHandler implements ScoreManager
{
    private final Object mutex = new Object();
    private final int SCORE_INCREMENT = 10;
    private final UserInterface ui;

    private int score;

    public ScoreHandler(UserInterface ui)
    {
        this.ui = ui;
        this.score = 0;
        updateScore();
    }

    public void incrementScore()
    {
        synchronized (mutex)
        {
            score += SCORE_INCREMENT;
        }

        updateScore();
    }

    public int enemyKilled(Entity enemyKilled, FireCommand killingShot)
    {
        long delayBonus = Math.abs(System.currentTimeMillis() - killingShot.getTimeInitiated());
        int enemyKilledBonus = SCORE_INCREMENT + (100 * (int)(delayBonus / enemyKilled.getDelayInMillis()));
        synchronized (mutex)
        {
            score += enemyKilledBonus;
        }

        updateScore();
        return enemyKilledBonus;
    }

    private void updateScore()
    {
        String scoreText;
        synchronized (mutex)
        {
            scoreText = String.valueOf(score);
        }

        ui.updateScore(scoreText);
    }
}
