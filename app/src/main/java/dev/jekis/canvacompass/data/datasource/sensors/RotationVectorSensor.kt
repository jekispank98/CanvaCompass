package dev.jekis.canvacompass.data.datasource.sensors

import android.hardware.Sensor
import android.hardware.SensorManager

/**
 * Implementation of the rotation vector sensor.
 * Provides device orientation data as a result of the synthesis of several sensors
 * (accelerometer, magnetometer, gyroscope). It is the most accurate way to determine orientation.
 *
 * @param SensorManager is a system service for working with Android sensors.
 */
class RotationVectorSensor(
    sensorManager: SensorManager
): AbstractSensor(sensorManager, Sensor.TYPE_ROTATION_VECTOR)