import java.math.BigInteger;
import java.util.Random;

/**
 * Contains a static method for getting random BigIntegers based off answers
 * found on <a
 * href="https://stackoverflow.com/questions/2290057/how-to-generate-a-random-biginteger-value-in-java">Stack
 * Overflow</a>.
 *
 * @author Sam K
 * @version 1/28/2025
 */
public class BigMath {
// Methods

    /**
     * Insecurely generates a random BigInteger.
     *
     * @param n The upper bound, exclusive.
     *
     * @return A random BigInteger in [1, n-1].
     */
    protected static BigInteger nextRandomBigInteger(BigInteger n) {
        n = n.subtract(BigInteger.ONE);
        Random rand = new Random();
        BigInteger result = new BigInteger(n.bitLength(), rand);
        while (result.compareTo(n) >= 0) {
            result = new BigInteger(n.bitLength(), rand);
        }
        return result.add(BigInteger.ONE);
    }
}
