package com.sinst.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Regression Tests: SINST 425 Automated Model & Validation Assertions")
public class ComprehensiveRegressionTest {

    @Test
    @DisplayName("Execute full historical test suite assertions")
    void testAllLegacyAndRegressionAssertions() {
        assertDoesNotThrow(() -> {
            SinstTestSuite.main(new String[0]);
        }, "Comprehensive regression test suite encountered failures");
    }
}
