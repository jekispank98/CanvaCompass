package dev.jekis.canvacompass.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin

@KoinApplication
class CanvaCompassApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin<CanvaCompassApp> {
            androidContext(this@CanvaCompassApp)
        }
    }
}