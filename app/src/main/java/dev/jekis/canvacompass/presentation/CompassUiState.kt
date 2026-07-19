package dev.jekis.canvacompass.presentation

import dev.jekis.canvacompass.domain.SensorAccuracy

data class CompassUiState(
    val azimuth: Float = 0f,
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val accuracy: SensorAccuracy = SensorAccuracy.UNRELIABLE,
    val isCalibrated: Boolean = false,
    val displayAzimuth: Int = 0,
    val directionRes: Int? = null,
    val showCalibrationWarning: Boolean = false
)
