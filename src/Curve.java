import java.math.BigInteger;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * A Curve represents any elliptic curve that can be expressed in short
 * Weierstrass form (y^2 = x^3 + ax + b).
 *
 * @author Sam K
 * @version 1/27/2025
 */
public class Curve {
// Attributes

    /**
     * The generator point of this Curve, as a Point.
     */
    protected final Point G;

    /**
     * The "a" coefficient of the short Weierstrass form of this Curve, as a
     * BigInteger.
     */
    private final BigInteger a;

    /**
     * The "b" coefficient of the short Weierstrass form of this Curve, as a
     * BigInteger.
     */
    private final BigInteger b;

    /**
     * The prime number to perform modular arithmetic over, as a BigInteger.
     */
    private final BigInteger p;

// Constructors

    /**
     * Constructs a new elliptic curve based on the given parameters.
     *
     * @param a The "a" coefficient of the short Weierstrass form of this Curve,
     *          as a BigInteger.
     * @param b The "b" coefficient of the short Weierstrass form of this Curve,
     *          as a BigInteger.
     * @param p The prime number to perform modular arithmetic over, as a
     *          BigInteger.
     * @param G The generator point of this Curve, as a Point.
     */
    protected Curve(BigInteger a, BigInteger b, BigInteger p, Point G) {
        this.a = a;
        this.b = b;
        this.p = p;
        this.G = G;
    }

// Methods

    /**
     * Adds two points on this Curve modulo p.
     *
     * @param pointA The first addend, as a Point.
     * @param pointB The second addend, as a Point.
     *
     * @return The resultant Point on this Curve.
     */
    protected Point add(Point pointA, Point pointB) {
        if (pointA.x().equals(pointB.x()) &&
            pointA.y().add(pointB.y()).mod(this.p).equals(BigInteger.ZERO)) {
            return Point.INFINITY;
        } else if (pointA.equals(Point.INFINITY)) {
            return pointB;
        } else if (pointB.equals(Point.INFINITY)) {
            return pointA;
        }
        BigInteger slope;
        if (pointA.equals(pointB)) {
            slope = ((pointA.x().pow(2).multiply(BigInteger.valueOf(3))
                            .add(this.a)).multiply(
                    (pointA.y().multiply(BigInteger.TWO)
                           .modInverse(this.p)))).mod(this.p);
        } else {
            slope = ((pointB.y().subtract(pointA.y())).multiply(
                    (pointB.x().subtract(pointA.x())).modInverse(this.p))).mod(
                    this.p);
        }
        BigInteger newX =
                ((slope.pow(2)).subtract(pointA.x()).subtract(pointB.x())).mod(
                        this.p);
        BigInteger newY =
                slope.multiply(pointA.x().subtract(newX)).subtract(pointA.y())
                     .mod(this.p);
        return new Point(newX, newY);
    }

    /**
     * Determines the Point(s) corresponding to a given x on this Curve.
     *
     * @param x The x-coordinate to find a Point on this Curve for, as a
     *          BigInteger.
     *
     * @return An array of Points.
     */
    protected Point[] computePoint(BigInteger x) {
        BigInteger yBase = ((((x.modPow(BigInteger.valueOf(3), p)).mod(p)
                                                                  .add(x.multiply(
                                                                                a)
                                                                        .mod(p))).mod(
                p).add(b)).mod(p));
        BigInteger yRoot = Modular.squareRootModP(yBase, p);
        if (yRoot == null) {
            return null;
        }
        BigInteger y1 = yRoot.mod(p);
        BigInteger y2 = (yRoot.negate()).mod(p);
        Point[] points;
        if (y1.equals(y2)) {
            points = new Point[1];
            points[0] = new Point(x, y1);
        } else {
            points = new Point[2];
            points[0] = new Point(x, y1);
            points[1] = new Point(x, y2);
        }
        return points;
    }

    /**
     * Performs ElGamal asymmetric decryption for an array of bytes over this
     * Curve.
     *
     * @param ciphertext The ciphertext, as an array of Point pairs (in
     *                   arrays).
     * @param d          The recipients private key, as a BigInteger.
     *
     * @return The cleartext, as an array of bytes.
     */
    protected byte[] decrypt(Point[][] ciphertext, BigInteger d) {
        byte[] cleartext = new byte[ciphertext.length];
        for (int i = 0; i < cleartext.length; i++) {
            cleartext[i] = this.decrypt(ciphertext[i], d);
        }
        return cleartext;
    }

    /**
     * Performs ElGamal asymmetric encryption for an array of bytes over this
     * Curve.
     *
     * @param cleartext The cleartext, as an array of bytes.
     * @param Q         The recipients public key, as a Point.
     *
     * @return The ciphertext, as an array of Point pairs (in arrays).
     */
    protected Point[][] encrypt(byte[] cleartext, Point Q) {
        Point[][] ciphertext = new Point[cleartext.length][2];

        for (int i = 0; i < cleartext.length; i++) {
            ciphertext[i] = this.encrypt(cleartext[i], Q);
        }

        return ciphertext;
    }

    /**
     * Multiply a Point on this Curve by a scalar using the double and add
     * algorithm.
     *
     * @param p The Point to multiply.
     * @param t The scalar to multiply by, as a BigInteger.
     *
     * @return The product, as a Point.
     */
    protected Point multiply(Point p, BigInteger t) {
        p = p.copy();
        Point result = Point.INFINITY;
        int m = t.bitLength();
        for (int i = 0; i <= m; i++) {
            if (t.testBit(i)) {
                result = add(p, result);
            }
            p = add(p, p);
        }
        return result;
    }

    /**
     * Determines the order of the cyclic group generated by a given Point on
     * this Curve in a naive way.
     *
     * @param p The base Point to find the order of.
     *
     * @return The order of the cyclic group generated by p, as a BigInteger.
     */
    protected BigInteger order(Point p) {
        BigInteger order = BigInteger.ZERO;
        BigInteger t = BigInteger.ZERO;
        boolean hasSeenInfinity = false;
        while (true) {
            Point q = this.multiply(p, t);
            if (q.equals(Point.INFINITY)) {
                if (!hasSeenInfinity) {
                    order = order.add(BigInteger.ONE);
                    hasSeenInfinity = true;
                } else {
                    break;
                }
            } else {
                order = order.add(BigInteger.ONE);
            }
            t = t.add(BigInteger.ONE);
        }
        return order;
    }

    /**
     * Performs ElGamal asymmetric decryption for a single byte over this
     * Curve.
     *
     * @param ciphertext The ciphertext, as a pair of Points in an array.
     * @param d          The recipients private key, as a BigInteger.
     *
     * @return The cleartext, as a byte.
     */
    private byte decrypt(Point[] ciphertext, BigInteger d) {
        Point M = this.subtract(ciphertext[1], this.multiply(ciphertext[0], d));
        return this.invMap(M);
    }

    /**
     * Performs ElGamal asymmetric encryption for a single byte over this
     * Curve.
     *
     * @param cleartext The cleartext, as a byte.
     * @param Q         The recipients public key, as a Point.
     *
     * @return The ciphertext, as a pair of Points in an array.
     */
    private Point[] encrypt(byte cleartext, Point Q) {
        BigInteger order = order(this.G);
        BigInteger k = BigMath.nextRandomBigInteger(order);
        Point C = this.multiply(this.G, k);
        Point M = this.map(cleartext);
        Point kQ = this.multiply(Q, k);
        Point D = this.add(M, kQ);
        return new Point[]{C, D};
    }

    /**
     * Generates a HashMap where the keys are Bytes and the values are Points .
     * Attempts to assign each Byte to a unique Point on this Curve that is not
     * the Point At Infinity. If this Curve does not have at least 256 unique
     * Points in the cyclic group, this won't work!
     *
     * @return A HashMap used to convert from Bytes to Points and back.
     */
    private HashMap<Byte, Point> generateMap() {
        HashMap<Byte, Point> points = new HashMap<>();
        byte x = Byte.MIN_VALUE;
        byte z = Byte.MIN_VALUE;
        while ((points.size() < 256)) {
            int y = x + 128;
            Point point = this.multiply(this.G, BigInteger.valueOf(y));
            if (point != null && !point.equals(Point.INFINITY)) {
                points.put(z, point);
                z++;
            }
            x++;
        }
        return points;
    }

    /**
     * Map a Point on this Curve to a byte.
     *
     * @param q The Point to map.
     *
     * @return The byte associated with this Point.
     *
     * @throws NoSuchElementException If no byte could be associated with the
     *                                given Point.
     */
    private byte invMap(Point q) {
        HashMap<Byte, Point> points = generateMap();

        byte index;
        for (index = Byte.MIN_VALUE; index <= Byte.MAX_VALUE; index++) {
            if (points.get(index).equals(q)) {
                return index;
            }
        }
        throw new NoSuchElementException(
                "The point " + q + " does not have " + "an associated byte!");
    }

    /**
     * Maps the given byte to a Point on this Curve.
     *
     * @param b The byte to map.
     *
     * @return The Point associated with this byte, or null if none was found
     */
    private Point map(byte b) {
        HashMap<Byte, Point> points = generateMap();

        return points.get(b);
    }

    /**
     * Subtracts two points on this Curve modulo p.
     *
     * @param pointA The minuend, as a Point.
     * @param pointB The subtrahend, as a Point.
     *
     * @return The difference, as a Point.
     */
    private Point subtract(Point pointA, Point pointB) {
        Point pointC = new Point(pointB.x(), pointB.y().negate().mod(this.p));
        return this.add(pointA, pointC);
    }
}
