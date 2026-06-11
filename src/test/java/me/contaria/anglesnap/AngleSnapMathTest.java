package me.contaria.anglesnap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AngleSnapMathTest {
    @Test
    void yawDistanceUsesShortestPathAcrossWrapBoundary() {
        assertEquals(2.0f, AngleSnapMath.yawDistance(-179.0f, 179.0f), 0.0001f);
    }

    @Test
    void angleDistanceClampsPitchBeforeComparing() {
        assertEquals(0.0f, AngleSnapMath.angleDistance(0.0f, 120.0f, 0.0f, 90.0f), 0.0001f);
    }

    @Test
    void wrapDegreesMatchesMinecraftStyleRange() {
        assertEquals(-180.0f, AngleSnapMath.wrapDegrees(180.0f), 0.0001f);
        assertEquals(179.0f, AngleSnapMath.wrapDegrees(-181.0f), 0.0001f);
    }
}
