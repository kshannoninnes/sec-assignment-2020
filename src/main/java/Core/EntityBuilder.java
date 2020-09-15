package Core;

import Interfaces.Spawner;
import Models.Entity;
import Models.Position;
import javafx.application.Platform;
import javafx.scene.image.Image;

import java.io.InputStream;
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
    private final Spawner spawner;
    private final List<Position> spawnLocations;

    public EntityBuilder(int spawnTimer, int startingId, Spawner spawner, List<Position> spawnLocations)
    {
        this.spawnTimer = spawnTimer;
        this.id = startingId;
        this.spawner = spawner;
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
                Platform.runLater(() -> spawner.spawn(newEntity));
            }

            Thread.sleep(spawnTimer);
        }
        catch (InterruptedException e)
        {
            System.out.println("Enemy spawner shutting down...");
        }
        catch (ExecutionException e)
        {
            System.out.printf("Exception: %s", e.getCause());
        }
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

    private Position getSpawnLocation() throws ExecutionException, InterruptedException
    {
        Position spawnLocation = null;
        CompletableFuture<List<Position>> validSpawnsFuture = new CompletableFuture<>();
        Platform.runLater(() -> validSpawnsFuture.complete(spawner.filterPositions(spawnLocations)));
        List<Position> validSpawns = validSpawnsFuture.get();
        if(validSpawns.size() > 0)
        {
            int index = ThreadLocalRandom.current().nextInt(validSpawns.size());
            spawnLocation = validSpawns.get(index);
        }

        return spawnLocation;
    }
}
