package dev.jekis.canvacompass

import org.junit.Assert.assertEquals
import org.junit.Test

class CompassUtilsTest {

    @Test
    fun `normalizeDegree should return same value if within 0 to 359`() {
        assertEquals(0f, CompassUtils.normalizeDegree(0f), 0.01f)
        assertEquals(180f, CompassUtils.normalizeDegree(180f), 0.01f)
        assertEquals(359.9f, CompassUtils.normalizeDegree(359.9f), 0.01f)
    }

    @Test
    fun `normalizeDegree should handle negative values`() {
        assertEquals(350f, CompassUtils.normalizeDegree(-10f), 0.01f)
        assertEquals(0f, CompassUtils.normalizeDegree(-360f), 0.01f)
    }

    @Test
    fun `normalizeDegree should handle values greater than 360`() {
        assertEquals(10f, CompassUtils.normalizeDegree(370f), 0.01f)
        assertEquals(0f, CompassUtils.normalizeDegree(720f), 0.01f)
    }
}
