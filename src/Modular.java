import java.math.BigInteger;

/**
 * Modular contains static methods for doing modular arithmetic on BigIntegers,
 * based on code found at <a
 * href="https://faculty.washington.edu/moishe/tcss581/modulararith/ModularArith.java">this
 * URL</a>.
 *
 * @author Sam K
 * @version 1/28/2025
 */
public class Modular {
// Methods

    /**
     * Calculates square root of residue modulo a prime.
     *
     * @param residue The residue, as a BigInteger.
     * @param p       The prime, as a BigInteger.
     *
     * @return The square root of residue mod p or null if none can be found.
     */
    protected static BigInteger squareRootModP(BigInteger residue,
                                               BigInteger p) {
        BigInteger zero = BigInteger.ZERO;
        BigInteger one = BigInteger.ONE;
        BigInteger two = BigInteger.TWO;

        if (p.mod(two).compareTo(zero) == 0) {
            return null;
        }

        BigInteger q = (p.subtract(one)).divide(two);

        if (residue.modPow(q, p).compareTo(one) != 0) {
            return null;
        }

        while (q.mod(two).compareTo(zero) == 0) {
            q = q.divide(two);
            if (residue.modPow(q, p).compareTo(one) != 0) {
                return exponentSquareRootModP(residue, q, p);
            }
        }

        q = (q.add(one)).divide(two);
        return residue.modPow(q, p);
    }

    /**
     * Calculates square root of residue modulo a prime using a start exponent
     * q.
     *
     * @param residue The residue, as a BigInteger.
     * @param q       The exponent, as a BigInteger.
     * @param p       The prime, as a BigInteger.
     *
     * @return square root of res mod p or null if none can be found
     */
    private static BigInteger exponentSquareRootModP(BigInteger residue,
                                                     BigInteger q,
                                                     BigInteger p) {
        BigInteger a = findNonResidue(p);
        if (a == null) {
            return null;
        }
        BigInteger zero = BigInteger.ZERO;
        BigInteger one = BigInteger.ONE;
        BigInteger two = BigInteger.TWO;
        BigInteger t = (p.subtract(one)).divide(two);
        BigInteger negativePower = t;

        while (q.mod(two).compareTo(zero) == 0) {
            q = q.divide(two);
            t = t.divide(two);
            if (residue.modPow(q, p).compareTo(a.modPow(t, p)) != 0) {
                t = t.add(negativePower);
            }
        }
        BigInteger inverseResidue = residue.modInverse(p);
        q = (q.subtract(one)).divide(two);

        t = t.divide(two);

        return inverseResidue.modPow(q, p).multiply(a.modPow(t, p)).mod(p);
    }

    /**
     * Finds the non residue of the prime p
     *
     * @param p The prime number, as a BigInteger.
     *
     * @return The non residue of p as a BigInteger, or null if one could not be
     * found.
     */
    private static BigInteger findNonResidue(BigInteger p) {
        BigInteger one = BigInteger.ONE;
        BigInteger two = BigInteger.TWO;
        int a = 2;
        BigInteger q = (p.subtract(one)).divide(two);
        while (true) {
            if (BigInteger.valueOf(a).modPow(q, p).compareTo(one) != 0) {
                return BigInteger.valueOf(a);
            }
            if (a == 0) {
                return null;
            }
            a++;
        }
    }
}
