package dev.jekis.canvacompass.di

import android.content.Context
import android.hardware.SensorManager
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("dev.jekis.canvacompass")
class KoinModule {
    @Single
    fun sensorManager(context: Context): SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
}
