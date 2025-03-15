import java.math.BigInteger;

/**
 * A Point represents a Point on an elliptic curve.
 *
 * @author Sam K
 * @version 1/27/2025
 */
public record Point(BigInteger x, BigInteger y) {
// Attributes

    /**
     * A constant for representing The Point At Infinity.
     */
    public static final Point INFINITY =
            new Point(BigInteger.valueOf(-1), BigInteger.valueOf(-1));

// Methods

    /**
     * Returns a Point with the same coordinates as this Point.
     *
     * @return A Point with the same coordinates as this Point.
     */
    public Point copy() {
        return new Point(this.x, this.y);
    }

    /**
     * Returns a String representation of this Point.
     *
     * @return A String representation of this Point.
     */
    @Override
    public String toString() {
        if (this.equals(Point.INFINITY)) {
            return "The Point At Infinity";
        } else {
            return "(" + this.x() + ", " + this.y() + ")";
        }
    }

    /**
     * Returns a boolean which is True if this Point and the given Point have
     * the same coordinates.
     *
     * @param point The Point to compare this Point to.
     *
     * @return True if the two Points are equal, False otherwise.
     */
    private boolean equals(Point point) {
        return this.x.compareTo(point.x()) == 0 &&
               this.y.compareTo(point.y()) == 0;
    }
}
