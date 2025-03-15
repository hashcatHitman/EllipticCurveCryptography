import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The Main class acts as scratch paper for testing functionality and ideas.
 *
 * @author Sam K
 * @version 1/29/2025
 */
public class Main {
// Methods

    /**
     * Runs test methods.
     */
    public static void main(String[] args) {
        // pointTest();
        // secp256k1PointMultiplication();
        usage(false);
    }

    /**
     * Generates an example curve. Finds all points on the curve, determines
     * their order (inefficiently), the total number of points, and demonstrates
     * addition and multiplication by a scalar.
     */
    private static void pointTest() {
        BigInteger a = BigInteger.valueOf(7);
        BigInteger b = BigInteger.valueOf(11);
        BigInteger p = BigInteger.valueOf(883);
        Point g = new Point(BigInteger.valueOf(7), BigInteger.valueOf(455));

        Curve curve = new Curve(a, b, p, g);
        ArrayList<Point> points = new ArrayList<>();

        for (int x = 0; x < p.intValueExact(); x++) {
            Point[] foundPoints = curve.computePoint(BigInteger.valueOf(x));
            if (foundPoints != null) {
                for (Point foundPoint : foundPoints) {
                    System.out.println("Point " + foundPoint + " has order:\t" +
                                       curve.order(foundPoint));
                }
                points.addAll(Arrays.stream(foundPoints).toList());
            }
        }
        System.out.println("Number of points found:\t" + points.size());

        for (Point A : points) {
            for (Point B : points) {
                Point C;
                System.out.print(A + " + " + B + " = ");
                try {
                    C = curve.add(A, B);
                    System.out.println(C);
                } catch (Exception _) {
                    System.out.println("Something went wrong!");
                }
            }
        }

        Point d = points.getFirst();
        for (int t = 0; t <= 7; t++) {
            Point q;
            System.out.print(t + " * " + d + " = ");
            try {
                q = curve.multiply(d, BigInteger.valueOf(t));
                System.out.println(q);
            } catch (Exception _) {
                System.out.println("Something went wrong!");
            }
        }
    }

    /**
     * Generates the secp256k1 curve and does some point multiplication of the
     * generator point.
     */
    private static void secp256k1PointMultiplication() {
        SECP256K1 sec = new SECP256K1();
        for (BigInteger t = BigInteger.ZERO; t.compareTo(BigInteger.TEN) < 0;
             t = t.add(BigInteger.ONE)) {
            System.out.println(t + "G:\t" + sec.multiply(SECP256K1.G, t));
        }
    }

    /**
     * Performs ElGamal and Diffie-Hellman over an elliptic curve.
     *
     * @param useWeakCurve If true, a hand-picked curve will be used. It's much
     *                     smaller and meant as a model for understanding with
     *                     less computational power. Otherwise, secp256k1 will
     *                     be used.
     */
    private static void usage(boolean useWeakCurve) {
        Curve curve;
        if (useWeakCurve) {
            BigInteger a = BigInteger.valueOf(7);
            BigInteger b = BigInteger.valueOf(11);
            BigInteger p = BigInteger.valueOf(883);
            Point g = new Point(BigInteger.valueOf(7), BigInteger.valueOf(455));
            curve = new Curve(a, b, p, g);
        } else {
            curve = new SECP256K1();
        }

        BigInteger order = curve.order(curve.G);
        BigInteger alicePrivate = BigMath.nextRandomBigInteger(order);

        Point alicePublic = curve.multiply(curve.G, alicePrivate);
        BigInteger bobPrivate = BigMath.nextRandomBigInteger(order);
        Point bobPublic = curve.multiply(curve.G, bobPrivate);
        String message = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxy" +
                         "z `~1!2@3#4$5%6^7&8*9(0)-_=+[{]}|\\:;\"',<.>/?";

        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        Point[][] ciphertext = curve.encrypt(messageBytes, bobPublic);
        byte[] cleartextBytes = curve.decrypt(ciphertext, bobPrivate);
        String cleartext = new String(cleartextBytes, StandardCharsets.UTF_8);
        System.out.println("Original Cleartext: \t" + cleartext);
        System.out.println("Ciphertext:\t" + Arrays.deepToString(ciphertext));
        System.out.println("Recovered Cleartext:\t" + cleartext);
        System.out.println(
                "Original Bytes: \t" + Arrays.toString(messageBytes));
        System.out.println(
                "Recovered Bytes:\t" + Arrays.toString(cleartextBytes));

        BigInteger bobShared = curve.multiply(alicePublic, bobPrivate).x();
        BigInteger aliceShared = curve.multiply(bobPublic, alicePrivate).x();
        System.out.println("Alice's Derived Secret:\t" + aliceShared);
        System.out.println("Bob's Derived Secret:\t" + aliceShared);
        System.out.println(
                "Shared secret identical:\t" + bobShared.equals(aliceShared));
    }
}
