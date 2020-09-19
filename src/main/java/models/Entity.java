package models;

import javafx.scene.image.Image;

/**
 * Immutable object to represent an entity within the game
 */
public class Entity
{
    private final int id;
    private final Image image;
    private final Position position;
    private final int delayInMillis;

    public Entity(int id, Image image, Position position, int delayInMillis)
    {

        this.id = id;
        this.image = image;
        this.position = position;
        this.delayInMillis = delayInMillis;
    }

    public int getId()
    {
        return id;
    }

    public Image getImage()
    {
        return image;
    }

    public Position getPosition()
    {
        return position;
    }

    public int getDelayInMillis()
    {
        return delayInMillis;
    }
}
