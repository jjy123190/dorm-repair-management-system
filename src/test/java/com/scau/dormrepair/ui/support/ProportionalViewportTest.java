package com.scau.dormrepair.ui.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ProportionalViewportTest {

    @Test
    void fillsTallViewportWithoutChangingAspectRatio() {
        ProportionalViewport.LayoutMetrics metrics =
                ProportionalViewport.resolveLayout(720, 900, 1440, 900);

        assertEquals(1.0, metrics.scale(), 0.000001);
        assertEquals(-360, metrics.offsetX(), 0.000001);
        assertEquals(0, metrics.offsetY(), 0.000001);
    }

    @Test
    void fillsWideViewportWithoutLeavingTopAndBottomGaps() {
        ProportionalViewport.LayoutMetrics metrics =
                ProportionalViewport.resolveLayout(1600, 900, 1440, 900);

        assertEquals(1600d / 1440d, metrics.scale(), 0.000001);
        assertEquals(0, metrics.offsetX(), 0.000001);
        assertEquals(-50, metrics.offsetY(), 0.000001);
    }

    @Test
    void returnsNullForInvalidViewport() {
        assertNull(ProportionalViewport.resolveLayout(0, 900, 1440, 900));
    }
}
