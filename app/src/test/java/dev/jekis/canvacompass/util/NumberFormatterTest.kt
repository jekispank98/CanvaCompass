package dev.jekis.canvacompass.util

import org.junit.Assert.assertEquals
import org.junit.Test

class NumberFormatterTest {

    @Test
    fun `normalizeAzimuth should return same value if within 0 to 359`() {
        assertEquals(0f, 0f.normalizeAzimuth(), 0.01f)
        assertEquals(180f, 180f.normalizeAzimuth(), 0.01f)
        assertEquals(359.9f, 359.9f.normalizeAzimuth(), 0.01f)
    }

    @Test
    fun `normalizeAzimuth should handle negative values`() {
        assertEquals(350f, (-10f).normalizeAzimuth(), 0.01f)
        assertEquals(0f, (-360f).normalizeAzimuth(), 0.01f)
    }

    @Test
    fun `normalizeAzimuth should handle values greater than 360`() {
        assertEquals(10f, 370f.normalizeAzimuth(), 0.01f)
        assertEquals(0f, 720f.normalizeAzimuth(), 0.01f)
    }
}
