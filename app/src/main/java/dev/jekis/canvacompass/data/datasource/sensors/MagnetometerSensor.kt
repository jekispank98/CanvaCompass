package dev.jekis.canvacompass.data.datasource.sensors

import android.hardware.Sensor
import android.hardware.SensorManager
import org.koin.core.annotation.Singleton

/**
 * Implementation of a magnetic field sensor (magnetometer).
 * Measures the strength of the surrounding magnetic field along three axes.
 * Used to determine the orientation of the device relative to magnetic north.
 *
 * @param SensorManager is a system service for working with Android sensors.
 */
@Singleton
class MagnetometerSensor(sensorManager: SensorManager) :
    AbstractSensor(sensorManager, Sensor.TYPE_MAGNETIC_FIELD)