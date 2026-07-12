package dev.jekis.canvacompass.util

/*
 * the azimuth after getOrientation can be negative, normalize to 0..360
 */

fun Float.normalizeAzimuth(): Float {
    val normalized = this % 360
    return if (normalized < 0) normalized + 360 else normalized
}
