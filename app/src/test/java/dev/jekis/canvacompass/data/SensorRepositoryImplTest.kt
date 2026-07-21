package dev.jekis.canvacompass.data

import android.content.Context
import android.hardware.SensorManager
import android.view.Surface
import app.cash.turbine.test
import dev.jekis.canvacompass.data.datasource.sensors.AccelerometerSensor
import dev.jekis.canvacompass.data.datasource.sensors.MagnetometerSensor
import dev.jekis.canvacompass.data.datasource.sensors.RotationVectorSensor
import dev.jekis.canvacompass.data.datasource.sensors.SensorState
import dev.jekis.canvacompass.domain.SensorAccuracy
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SensorRepositoryImplTest {

    private val sensorManager: SensorManager = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private val accelerometerSensor: AccelerometerSensor = mockk(relaxed = true)
    private val magnetometerSensor: MagnetometerSensor = mockk(relaxed = true)
    private val rotationVectorSensor: RotationVectorSensor = mockk(relaxed = true)

    private var rotationValue: Int = Surface.ROTATION_0
    private lateinit var repository: SensorRepositoryImpl

    private val accelState = MutableStateFlow(SensorState(name = "Accel"))
    private val magState = MutableStateFlow(SensorState(name = "Mag"))
    private val rotationState = MutableStateFlow(SensorState(name = "Rotation"))

    @Before
    fun setup() {
        mockkStatic(SensorManager::class)
        every { SensorManager.remapCoordinateSystem(any(), any(), any(), any()) } returns true
        every { SensorManager.getRotationMatrixFromVector(any(), any()) } returns Unit
        every { SensorManager.getOrientation(any(), any()) } returns floatArrayOf(0f, 0f, 0f)
        every { SensorManager.getRotationMatrix(any(), any(), any(), any()) } returns true

        every { accelerometerSensor.sensorState } returns accelState
        every { magnetometerSensor.sensorState } returns magState
        every { rotationVectorSensor.sensorState } returns rotationState

        repository = SensorRepositoryImpl(
            sensorManager = sensorManager,
            context = context,
            accelerometerSensor = accelerometerSensor,
            magnetometerSensor = magnetometerSensor,
            rotationVectorSensor = rotationVectorSensor,
            rotationProvider = { rotationValue }
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `mapAccuracy should return correct enum values`() {
        assertEquals(SensorAccuracy.HIGH, repository.mapAccuracy(SensorManager.SENSOR_STATUS_ACCURACY_HIGH))
        assertEquals(SensorAccuracy.MEDIUM, repository.mapAccuracy(SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM))
        assertEquals(SensorAccuracy.LOW, repository.mapAccuracy(SensorManager.SENSOR_STATUS_ACCURACY_LOW))
        assertEquals(SensorAccuracy.UNRELIABLE, repository.mapAccuracy(SensorManager.SENSOR_STATUS_UNRELIABLE))
        assertEquals(SensorAccuracy.UNRELIABLE, repository.mapAccuracy(null))
    }

    @Test
    fun `remapMatrixBasedOnRotation should use correct axes for ROTATION_90`() {
        rotationValue = Surface.ROTATION_90
        val inputMatrix = FloatArray(9)

        repository.remapMatrixBasedOnRotation(inputMatrix)

        verify {
            SensorManager.remapCoordinateSystem(
                inputMatrix,
                SensorManager.AXIS_Y,
                SensorManager.AXIS_MINUS_X,
                any()
            )
        }
    }

    @Test
    fun `remapMatrixBasedOnRotation should use correct axes for ROTATION_180`() {
        rotationValue = Surface.ROTATION_180
        val inputMatrix = FloatArray(9)

        repository.remapMatrixBasedOnRotation(inputMatrix)

        verify {
            SensorManager.remapCoordinateSystem(
                inputMatrix,
                SensorManager.AXIS_MINUS_X,
                SensorManager.AXIS_MINUS_Y,
                any()
            )
        }
    }

    @Test
    fun `remapMatrixBasedOnRotation should use correct axes for ROTATION_270`() {
        rotationValue = Surface.ROTATION_270
        val inputMatrix = FloatArray(9)

        repository.remapMatrixBasedOnRotation(inputMatrix)

        verify {
            SensorManager.remapCoordinateSystem(
                inputMatrix,
                SensorManager.AXIS_MINUS_Y,
                SensorManager.AXIS_X,
                any()
            )
        }
    }

    @Test
    fun `remapMatrixBasedOnRotation should use default axes for ROTATION_0`() {
        rotationValue = Surface.ROTATION_0
        val inputMatrix = FloatArray(9)

        repository.remapMatrixBasedOnRotation(inputMatrix)

        verify {
            SensorManager.remapCoordinateSystem(
                inputMatrix,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Y,
                any()
            )
        }
    }

    @Test
    fun `fetchOrientationData should use RotationVector when available`() = runTest {
        every { rotationVectorSensor.isAvailable() } returns true
        
        repository.fetchOrientationData().test {
            rotationState.value = SensorState(name = "Rotation", values = floatArrayOf(0.1f, 0.2f, 0.3f))
            
            val item = awaitItem()
            assertEquals(SensorAccuracy.UNRELIABLE, item.accuracy)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchOrientationData should use Accel and Mag when RotationVector is NOT available`() = runTest {
        every { rotationVectorSensor.isAvailable() } returns false
        
        repository.fetchOrientationData().test {
            accelState.value = SensorState(name = "Accel", values = floatArrayOf(0f, 0f, 9.8f))
            magState.value = SensorState(name = "Mag", values = floatArrayOf(0f, 1f, 0f), accuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
            
            val item = awaitItem()
            assertEquals(SensorAccuracy.HIGH, item.accuracy)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
