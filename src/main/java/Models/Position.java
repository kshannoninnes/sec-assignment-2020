package Models;

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
     * Two Positions are considered equal if the whole integer floor of both of them are equal
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
}
