package dev.jekis.canvacompass

object CompassUtils {
    /**
     * Normalizes an angle to be within [0, 360) degrees.
     */
    fun normalizeDegree(degree: Float): Float {
        val normalized = degree % 360
        return if (normalized < 0) normalized + 360 else normalized
    }
}
