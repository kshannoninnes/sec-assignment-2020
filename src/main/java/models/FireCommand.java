package models;

public class FireCommand
{
    private final long timeInitiated;
    private final Position attackLocation;

    public FireCommand(long timeInitiated, Position attackLocation)
    {
        this.timeInitiated = timeInitiated;
        this.attackLocation = attackLocation;
    }

    public long getTimeInitiated()
    {
        return timeInitiated;
    }

    public Position getAttackLocation()
    {
        return new Position(attackLocation.getX(), attackLocation.getY());
    }
}
