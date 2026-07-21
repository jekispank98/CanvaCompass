package dev.jekis.canvacompass.presentation

import app.cash.turbine.test
import dev.jekis.canvacompass.domain.CompassOrientation
import dev.jekis.canvacompass.domain.GetCompassOrientationUseCase
import dev.jekis.canvacompass.domain.SensorAccuracy
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompassViewModelTest {

    private val useCase: GetCompassOrientationUseCase = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private val orientationFlow = MutableStateFlow(createOrientation(0f))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { useCase.invoke() } returns orientationFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when accuracy is LOW or UNRELIABLE, calibration warning should be shown`() = runTest {
        val viewModel = CompassViewModel(useCase)

        viewModel.uiState.test {
            // Initial state
            assertEquals(SensorAccuracy.UNRELIABLE, awaitItem().accuracy)
            
            // UNRELIABLE -> Warning shown
            orientationFlow.value = createOrientation(0f, accuracy = SensorAccuracy.UNRELIABLE)
            assertTrue(awaitItem().showCalibrationWarning)

            // LOW -> Warning shown
            orientationFlow.value = createOrientation(0f, accuracy = SensorAccuracy.LOW)
            assertTrue(awaitItem().showCalibrationWarning)

            // MEDIUM -> Warning hidden
            orientationFlow.value = createOrientation(0f, accuracy = SensorAccuracy.MEDIUM)
            assertFalse(awaitItem().showCalibrationWarning)

            // HIGH -> Warning hidden
            orientationFlow.value = createOrientation(0f, accuracy = SensorAccuracy.HIGH)
            assertFalse(awaitItem().showCalibrationWarning)
        }
    }

    @Test
    fun `azimuth should be smoothed across multiple updates`() = runTest {
        val viewModel = CompassViewModel(useCase)

        viewModel.uiState.test {
            awaitItem() // Initial

            // Move from 0 to 100. With factor 0.25, first step: 0 + (100-0)*0.25 = 25
            orientationFlow.value = createOrientation(100f)
            assertEquals(25f, awaitItem().azimuth, 0.1f)

            // Second step: 25 + (100-25)*0.25 = 25 + 18.75 = 43.75
            orientationFlow.value = createOrientation(100f)
            assertEquals(43.75f, awaitItem().azimuth, 0.1f)
        }
    }

    @Test
    fun `smoothing should handle 360 to 0 wrap around correctly`() = runTest {
        val viewModel = CompassViewModel(useCase)

        viewModel.uiState.test {
            awaitItem() // Initial

            // Start near 350
            orientationFlow.value = createOrientation(350f)
            val state1 = awaitItem() 
            // 0 + (350-0)*0.25 is wrong because it should go through 0.
            // Our logic: diff = 350 - 0 = 350. Since > 180, diff = 350 - 360 = -10.
            // New azimuth = 0 + (-10)*0.25 = -2.5 -> 357.5
            assertEquals(357.5f, state1.azimuth, 0.1f)

            // Jump to 10 (short path is forward across the 360 boundary)
            // current = 10, last = 357.5. diff = 10 - 357.5 = -347.5
            // Since abs(-347.5) > 180 and diff < 0, diff = -347.5 + 360 = 12.5
            // New azimuth = 357.5 + 12.5 * 0.25 = 357.5 + 3.125 = 360.625 -> 0.625
            orientationFlow.value = createOrientation(10f)
            assertEquals(0.625f, awaitItem().azimuth, 0.1f)
        }
    }

    private fun createOrientation(
        azimuth: Float,
        accuracy: SensorAccuracy = SensorAccuracy.HIGH
    ) = CompassOrientation(
        azimuth = azimuth,
        trueAzimuth = null,
        pitch = 0f,
        roll = 0f,
        accuracy = accuracy,
        magneticDeclination = 0f,
        timestamp = System.currentTimeMillis(),
        isCalibrated = true
    )
}
