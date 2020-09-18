package Models;


import javafx.scene.image.Image;

/**
 * Immutable object to represent an entity in the game.
 */
public class MovableEntity extends Entity
{
    private final int delayInMillis;

    public MovableEntity(int id, Image image, Position position, int delayInMillis)
    {
        super(id, image, position);
        this.delayInMillis = delayInMillis;
    }

    public int getDelayInMillis()
    {
        return delayInMillis;
    }
}
