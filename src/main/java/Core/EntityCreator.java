package Core;

import Interfaces.FilterPositions;
import Models.MovableEntity;
import Models.Position;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class EntityCreator
{
    private final String IMAGE_FILE = "redsphere.png";
    private final int DELAY_UPPER_BOUND = 2000;
    private final int DELAY_LOWER_BOUND = 500;

    private int id;
    private final Image entityImage;
    private final FilterPositions filter;
    private final List<Position> spawnLocations;

    public EntityCreator(List<Position> spawnLocations, FilterPositions filter)
    {
        this.id = 1;
        this.filter = filter;
        this.spawnLocations = spawnLocations;
        this.entityImage = createImage();
    }

    public MovableEntity getEntity()
    {
        Position spawnLocation = getSpawnLocation();
        if(spawnLocation == null) return null;

        int randomDelay = ThreadLocalRandom.current().nextInt(DELAY_LOWER_BOUND, DELAY_UPPER_BOUND);

        return new MovableEntity(id++, entityImage, spawnLocation, randomDelay);
    }

    private Image createImage()
    {
        InputStream is = getClass().getClassLoader().getResourceAsStream(IMAGE_FILE);
        if(is == null)
        {
            throw new AssertionError("Cannot find image file " + IMAGE_FILE);
        }

        return new Image(is);
    }

    private Position getSpawnLocation()
    {
        Position spawnLocation = null;
        List<Position> validSpawns = filter.filter(Collections.unmodifiableList(spawnLocations));
        if(validSpawns.size() > 0)
        {
            int index = ThreadLocalRandom.current().nextInt(validSpawns.size());
            spawnLocation = validSpawns.get(index);
        }

        return spawnLocation;
    }
}
