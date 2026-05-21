package com.sme.pm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SmePmApplicationTests {

    @Test
    void applicationClassExists() {
        // Simple test to verify the application class can be instantiated
        SmePmApplication app = new SmePmApplication();
        assertTrue(app != null);
    }
}
