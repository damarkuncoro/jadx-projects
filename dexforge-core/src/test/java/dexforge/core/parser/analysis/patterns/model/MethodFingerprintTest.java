package dexforge.core.parser.analysis.patterns.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class MethodFingerprintTest {

    @Test
    void testFingerprintCounting() {
        MethodFingerprint fingerprint = new MethodFingerprint();

        assertThat(fingerprint.getTotalInstructions()).isEqualTo(0);
        assertThat(fingerprint.getBitwiseDensity()).isEqualTo(0.0);

        fingerprint.addBitwise();
        fingerprint.addBitwise();
        fingerprint.addArithmetic();
        fingerprint.addInvoke();
        fingerprint.addArrayOp();
        fingerprint.addOther();

        assertThat(fingerprint.getBitwiseCount()).isEqualTo(2);
        assertThat(fingerprint.getArithmeticCount()).isEqualTo(1);
        assertThat(fingerprint.getInvokeCount()).isEqualTo(1);
        assertThat(fingerprint.getArrayOpCount()).isEqualTo(1);
        assertThat(fingerprint.getTotalInstructions()).isEqualTo(6);

        // Bitwise density: 2 / 6 = ~0.33333
        assertThat(fingerprint.getBitwiseDensity()).isCloseTo(0.33333, within(0.0001));
    }
}
