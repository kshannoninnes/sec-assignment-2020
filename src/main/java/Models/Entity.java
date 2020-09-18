package Models;

import javafx.scene.image.Image;

public abstract class Entity
{
    private final int id;
    private final Image image;
    private final Position position;

    public Entity(int id, Image image, Position position)
    {

        this.id = id;
        this.image = image;
        this.position = position;
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

    abstract int getDelayInMillis();
}
