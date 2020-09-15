package Models;


import javafx.scene.image.Image;

/**
 * Immutable object to represent an entity in the game.
 */
public class Entity
{
    private final int id;
    private final Image image;
    private final int delayInMillis;
    private final Position currentPosition;

    public Entity(int id, int delayInMillis, Image image, Position position)
    {
        this.id = id;
        this.image = image;
        this.delayInMillis = delayInMillis;
        this.currentPosition = position;
    }

    public int getId()
    {
        return id;
    }

    public Image getImage()
    {
        return image;
    }

    public int getDelayInMillis()
    {
        return delayInMillis;
    }

    public Position getPosition()
    {
        return new Position(currentPosition.getX(), currentPosition.getY());
    }
}
