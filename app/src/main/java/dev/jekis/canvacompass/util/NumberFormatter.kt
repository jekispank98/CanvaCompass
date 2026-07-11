package dev.jekis.canvacompass.util

/*
 * the azimuth after getOrientation can be negative, normalize to 0..360
 */

fun Float.normalizeAzimuth(): Float = (this + 360) % 360