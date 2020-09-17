package Core;

import Interfaces.FilterPositions;
import Interfaces.Log;
import Interfaces.AddEntity;
import Models.Entity;
import Models.Position;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class EntityBuilder implements Runnable
{
    private final String IMAGE_FILE = "redsphere.png";
    private final int DELAY_UPPER_BOUND = 2000;
    private final int DELAY_LOWER_BOUND = 500;

    private int id;
    private final Log logger;
    private final Image entityImage;
    private final int spawnTimer;
    private final AddEntity adder;
    private final FilterPositions filter;
    private final List<Position> spawnLocations;

    public EntityBuilder(int spawnTimer, List<Position> spawnLocations, Log logger, FilterPositions filter, AddEntity adder)
    {
        this.id = 1;
        this.spawnTimer = spawnTimer;
        this.logger = logger;
        this.adder = adder;
        this.filter = filter;
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
                adder.add(newEntity);
                logger.log(String.format("Entity #%d spawned with delay %d.", newEntity.getId(), newEntity.getDelayInMillis()));
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
        List<Position> validSpawns = filter.filter(Collections.unmodifiableList(spawnLocations));
        if(validSpawns.size() > 0)
        {
            int index = ThreadLocalRandom.current().nextInt(validSpawns.size());
            spawnLocation = validSpawns.get(index);
        }

        return spawnLocation;
    }
}
