package dev.jekis.canvacompass.data.datasource.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class AbstractSensor(
    protected val sensorManager: SensorManager,
    private val sensorType: Int
) {
    private val sensor: Sensor? = sensorManager.getDefaultSensor(sensorType)
    private val _sensorState = MutableStateFlow(SensorState(name = sensor?.name))
    val sensorState: StateFlow<SensorState> = _sensorState.asStateFlow()

    private val sensorListener = object : SensorEventListener2 {
        override fun onFlushCompleted(sensor: Sensor?) {
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            _sensorState.update { state -> state.copy(accuracy = accuracy) }
        }

        override fun onSensorChanged(event: SensorEvent?) {
            event?.let { event ->
                _sensorState.update { state ->
                    state.copy(
                        values = event.values.clone(),
                        accuracy = event.accuracy,
                        timestamp = event.timestamp
                    )
                }
            }?: return
        }
    }

    init {
        startListening()
    }

    fun isAvailable(): Boolean = sensor != null

    fun startListening() {
        sensor?.let {
            sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensor?.let {
            sensorManager.unregisterListener(sensorListener)
        }
    }
}