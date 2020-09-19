package core.handlers;

import interfaces.BoardManager;
import interfaces.LogManager;
import interfaces.SpawnManager;
import models.Entity;
import models.Position;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class SpawnHandler implements SpawnManager
{
    private final String IMAGE_FILE = "redsphere.png";
    private final List<Position> spawnLocations;
    private final int DELAY_UPPER_BOUND = 2000;
    private final int DELAY_LOWER_BOUND = 500;

    private int id;
    private final Image entityImage;

    private final BoardManager board;
    private final LogManager logger;

    public SpawnHandler(LogManager logger, BoardManager board, List<Position> spawnLocations)
    {
        this.id = 1;
        this.logger = logger;
        this.board = board;
        this.spawnLocations = spawnLocations;
        this.entityImage = createImage();
    }

    public Entity spawnEntity()
    {
        Position spawnLocation = getSpawnLocation();
        if(spawnLocation == null) return null;

        int randomDelay = ThreadLocalRandom.current().nextInt(DELAY_LOWER_BOUND, DELAY_UPPER_BOUND);
        Entity newEntity = new Entity(id++, entityImage, spawnLocation, randomDelay);
        board.addEntity(newEntity);
        logger.log(String.format("Entity #%d spawned with delay %d.", newEntity.getId(), newEntity.getDelayInMillis()));

        return newEntity;
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
        List<Position> validSpawns = board.filterPositions(Collections.unmodifiableList(spawnLocations));
        if(validSpawns.size() > 0)
        {
            int index = ThreadLocalRandom.current().nextInt(validSpawns.size());
            spawnLocation = validSpawns.get(index);
        }

        return spawnLocation;
    }
}
