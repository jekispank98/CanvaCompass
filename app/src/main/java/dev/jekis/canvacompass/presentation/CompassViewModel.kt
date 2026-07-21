package dev.jekis.canvacompass.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jekis.canvacompass.domain.GetCompassOrientationUseCase
import dev.jekis.canvacompass.domain.SensorAccuracy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.math.abs

private const val SMOOTHING_FACTOR = 0.25f

@KoinViewModel
class CompassViewModel(
    private val getCompassOrientationUseCase: GetCompassOrientationUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(CompassUiState())
    val uiState: StateFlow<CompassUiState> = _uiState.asStateFlow()

    private var lastAzimuth = 0f

    init {
        viewModelScope.launch {
            getCompassOrientationUseCase.invoke().collect { data ->
                val smoothedAzimuth = smoothAzimuth(data.azimuth)
                
                _uiState.update { state ->
                    state.copy(
                        azimuth = smoothedAzimuth,
                        pitch = data.pitch,
                        roll = data.roll,
                        accuracy = data.accuracy,
                        isCalibrated = data.isCalibrated,
                        displayAzimuth = data.azimuth.toInt(),
                        showCalibrationWarning = data.accuracy == SensorAccuracy.UNRELIABLE || 
                                               data.accuracy == SensorAccuracy.LOW
                    )
                }
            }
        }
    }

    private fun smoothAzimuth(current: Float): Float {
        var diff = current - lastAzimuth
        
        // Handle 360 -> 0 transition
        if (abs(diff) > 180) {
            if (diff > 0) diff -= 360f else diff += 360f
        }
        
        val smoothed = (lastAzimuth + diff * SMOOTHING_FACTOR) % 360f
        lastAzimuth = if (smoothed < 0) smoothed + 360f else smoothed
        return lastAzimuth
    }
}
