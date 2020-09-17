package Core;

import Models.Entity;
import Models.FireCommand;
import javafx.application.Platform;
import javafx.scene.control.Label;

public class ScoreHandler
{
    private final Object mutex = new Object();
    private final int SCORE_INCREMENT = 10;
    private final Label scoreDisplay;

    private int score;

    public ScoreHandler(Label scoreDisplay)
    {
        this.scoreDisplay = scoreDisplay;
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

    public void enemyKilled(Entity enemyKilled, FireCommand killingShot)
    {
        long delayBonus = Math.abs(System.currentTimeMillis() - killingShot.getTimeInitiated());
        int enemyKilledBonus = 100 * (int)(delayBonus / enemyKilled.getDelayInMillis());
        synchronized (mutex)
        {
            score += enemyKilledBonus + SCORE_INCREMENT;
        }

        updateScore();
    }

    private void updateScore()
    {
        String scoreText;
        synchronized (mutex)
        {
            scoreText = String.valueOf(score);
        }

        Platform.runLater(() -> scoreDisplay.setText(scoreText));
    }
}
