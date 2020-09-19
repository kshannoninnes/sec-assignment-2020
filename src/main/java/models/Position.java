package models;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Immutable object to represent a position on the game grid
 * Note: BigDecimal is used for precision purposes
 */
public class Position
{
    private final BigDecimal x;
    private final BigDecimal y;

    public Position(BigDecimal x, BigDecimal y)
    {
        this.x = x;
        this.y = y;
    }

    public BigDecimal getX()
    {
        return x;
    }

    public BigDecimal getY()
    {
        return y;
    }

    /**
     * Two Positions are generally considered equal if the difference between them is less than 1
     */
    @Override
    public boolean equals(Object obj)
    {
        if(obj == this) return true;
        if(!(obj instanceof Position)) return false;

        Position p = (Position)obj;

        BigDecimal xDiff = new BigDecimal(p.getX().subtract(this.getX()).abs().toString());
        BigDecimal yDiff = new BigDecimal(p.getY().subtract(this.getY()).abs().toString());

        boolean xEqual = xDiff.setScale(0, RoundingMode.FLOOR).compareTo(BigDecimal.ONE) < 0;
        boolean yEqual = yDiff.setScale(0, RoundingMode.FLOOR).compareTo(BigDecimal.ONE) < 0;

        return xEqual && yEqual;
    }

    public boolean exactEquals(Object obj)
    {
        if(obj == this) return true;
        if(!(obj instanceof Position)) return false;

        Position p = (Position)obj;

        boolean xEqual = p.getX().setScale(1, RoundingMode.HALF_EVEN).equals(this.getX());
        boolean yEqual = p.getY().setScale(1, RoundingMode.HALF_EVEN).equals(this.getY());

        return xEqual && yEqual;
    }
}
