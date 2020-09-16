package Core;

import Interfaces.FilterPositions;
import Interfaces.SpawnEntity;
import Models.Entity;
import Models.Position;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class EntityBuilder implements Runnable
{
    private final String IMAGE_FILE = "enemy.png";
    private final int DELAY_UPPER_BOUND = 2000;
    private final int DELAY_LOWER_BOUND = 500;

    private int id;
    private final Image entityImage;
    private final int spawnTimer;
    private final SpawnEntity spawner;
    private final FilterPositions positionFilter;
    private final List<Position> spawnLocations;

    public EntityBuilder(int spawnTimer, int startingId, SpawnEntity spawner, FilterPositions positionFilter, List<Position> spawnLocations)
    {
        this.spawnTimer = spawnTimer;
        this.id = startingId;
        this.spawner = spawner;
        this.positionFilter = positionFilter;
        this.spawnLocations = spawnLocations;
        this.entityImage = createImage();
    }

    @Override
    public void run()
    {
        try
        {
            int randomDelay = ThreadLocalRandom.current().nextInt(DELAY_LOWER_BOUND, DELAY_UPPER_BOUND);
            Position spawnLocation = getSpawnLocation();
            if(spawnLocation != null)
            {
                Entity newEntity = new Entity(id++, randomDelay, entityImage, spawnLocation);
                spawner.spawn(newEntity);
            }

            Thread.sleep(spawnTimer);
        }
        catch (InterruptedException e) { /* Shutting down... */ }
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
        List<Position> validSpawns = positionFilter.filter(Collections.unmodifiableList(spawnLocations));
        if(validSpawns.size() > 0)
        {
            int index = ThreadLocalRandom.current().nextInt(validSpawns.size());
            spawnLocation = validSpawns.get(index);
        }

        return spawnLocation;
    }
}
