package dev.jekis.canvacompass.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jekis.canvacompass.domain.GetCompassOrientationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class CompassViewModel(
    private val getCompassOrientationUseCase: GetCompassOrientationUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(CompassUiState())
    val uiState: StateFlow<CompassUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getCompassOrientationUseCase.invoke().collect { data ->
                _uiState.update { state ->
                    state.copy(
                        azimuth = data.azimuth,
                        pitch = data.pitch,
                        roll = data.roll,
                        accuracy = data.accuracy,
                        isCalibrated = data.isCalibrated
                    )
                }
            }
        }
    }
}