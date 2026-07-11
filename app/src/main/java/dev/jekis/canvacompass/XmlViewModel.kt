package dev.jekis.canvacompass

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jekis.canvacompass.domain.GetCompassOrientationUseCase
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class XmlViewModel(
    private val getCompassOrientation: GetCompassOrientationUseCase
) : ViewModel() {

    init {
        viewModelScope.launch {
            getCompassOrientation.invoke().collect { state ->
                Log.d("check_state", "$state")
            }
        }
    }
}