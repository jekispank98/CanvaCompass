package dev.jekis.canvacompass.domain

/**
 * Domain model describing the current device orientation for the compass.
 *
 * @param azimuth direction to magnetic north in degrees, range 0..360
 * @param trueAzimuth direction to true (geographic) north in degrees,
 *                    adjusted for magnetic declination; null if the user's
 *                    location is unavailable and the correction cannot be computed
 * @param pitch forward/backward tilt angle of the device in degrees, range -180..180
 * @param roll left/right tilt angle of the device in degrees, range -90..90
 * @param accuracy current accuracy level of the magnetometer readings
 * @param magneticDeclination magnetic declination correction in degrees,
 *                             calculated for the user's current coordinates
 * @param timestamp time of the measurement in milliseconds (System.currentTimeMillis()),
 *                  used for smoothing and discarding stale readings
 * @param isCalibrated indicates whether the magnetometer is calibrated
 *                      and the readings are reliable
 */
data class CompassOrientation(
    val azimuth: Float,
    val trueAzimuth: Float?,
    val pitch: Float,
    val roll: Float,
    val accuracy: SensorAccuracy,
    val magneticDeclination: Float,
    val timestamp: Long,
    val isCalibrated: Boolean
)

/**
 * Accuracy level of magnetometer readings, as reported by the Android Sensor Framework.
 *
 * @property UNRELIABLE readings are unreliable, calibration is required
 * @property LOW low accuracy, calibration is recommended
 * @property MEDIUM medium accuracy, acceptable for most use cases
 * @property HIGH high accuracy, readings are reliable
 */
enum class SensorAccuracy {
    UNRELIABLE, LOW, MEDIUM, HIGH
}
