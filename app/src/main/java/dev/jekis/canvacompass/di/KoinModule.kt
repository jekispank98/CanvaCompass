package dev.jekis.canvacompass.di

import android.content.Context
import android.hardware.SensorManager
import dev.jekis.canvacompass.data.datasource.sensors.AccelerometerSensor
import dev.jekis.canvacompass.data.datasource.sensors.MagnetometerSensor
import dev.jekis.canvacompass.data.datasource.sensors.RotationVectorSensor
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("dev.jekis.canvacompass")
class KoinModule {
    @Single
    fun sensorManager(context: Context): SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    @Single
    fun accelerometerSensor(sensorManager: SensorManager) = AccelerometerSensor(sensorManager)

    @Single
    fun magnetometerSensor(sensorManager: SensorManager) = MagnetometerSensor(sensorManager)

    @Single
    fun rotationVectorSensor(sensorManager: SensorManager) = RotationVectorSensor(sensorManager)
}
