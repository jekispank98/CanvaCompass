package dev.jekis.canvacompass.data

import android.content.Context
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.Surface
import androidx.annotation.VisibleForTesting
import androidx.core.content.getSystemService
import dev.jekis.canvacompass.data.datasource.sensors.AccelerometerSensor
import dev.jekis.canvacompass.data.datasource.sensors.MagnetometerSensor
import dev.jekis.canvacompass.data.datasource.sensors.RotationVectorSensor
import dev.jekis.canvacompass.domain.CompassOrientation
import dev.jekis.canvacompass.domain.SensorAccuracy
import dev.jekis.canvacompass.domain.SensorRepository
import dev.jekis.canvacompass.util.normalizeAzimuth
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import org.koin.core.annotation.Single

private const val DATA_INTERVAL = 40L
@Single
class SensorRepositoryImpl(
    private val sensorManager: SensorManager,
    private val context: Context,
    private val accelerometerSensor: AccelerometerSensor,
    private val magnetometerSensor: MagnetometerSensor,
    private val rotationVectorSensor: RotationVectorSensor,
    private val rotationProvider: () -> Int = { getScreenRotation(context) }
) : SensorRepository {

    @OptIn(FlowPreview::class)
    override fun fetchOrientationData(): Flow<CompassOrientation> {
        val orientationFlow = if (rotationVectorSensor.isAvailable()) {
            rotationVectorSensor.sensorState
                .filter { it.values.isNotEmpty() }
                .conflate()
                .map { fuseFromRotationVector(it.values) }
        } else {
            combine(
                accelerometerSensor.sensorState.filter { it.values.isNotEmpty() },
                magnetometerSensor.sensorState.filter { it.values.isNotEmpty() }
            ) { accel, mag -> fuseFromAccelMag(accel.values, mag.values) }
                .conflate()
        }

        return combine(
            orientationFlow,
            magnetometerSensor.sensorState
        ) { orientation, magState ->
            orientation.copy(
                accuracy = mapAccuracy(magState.accuracy),
                isCalibrated = (magState.accuracy
                    ?: 0) >= SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
            )
        }
            .distinctUntilChanged { old, new ->
                old.azimuth == new.azimuth &&
                        old.pitch == new.pitch &&
                        old.roll == new.roll &&
                        old.isCalibrated == new.isCalibrated
            }
            .sample(DATA_INTERVAL)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun remapMatrixBasedOnRotation(rotationMatrix: FloatArray): FloatArray {
        val remappedMatrix = FloatArray(9)
        val worldAxisForDeviceAxisX: Int
        val worldAxisForDeviceAxisY: Int

        when (rotationProvider()) {
            Surface.ROTATION_90 -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_Y
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X
            }
            Surface.ROTATION_180 -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Y
            }
            Surface.ROTATION_270 -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Y
                worldAxisForDeviceAxisY = SensorManager.AXIS_X
            }
            else -> {
                worldAxisForDeviceAxisX = SensorManager.AXIS_X
                worldAxisForDeviceAxisY = SensorManager.AXIS_Y
            }
        }

        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            worldAxisForDeviceAxisX,
            worldAxisForDeviceAxisY,
            remappedMatrix
        )
        return remappedMatrix
    }

    private fun fuseFromRotationVector(values: FloatArray): CompassOrientation {
        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)

        if (values.isNotEmpty()) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
        }

        val remappedMatrix = remapMatrixBasedOnRotation(rotationMatrix)
        SensorManager.getOrientation(remappedMatrix, orientationAngles)

        return CompassOrientation(
            azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat().normalizeAzimuth(),
            trueAzimuth = null,
            pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat(),
            roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat(),
            accuracy = SensorAccuracy.UNRELIABLE,
            magneticDeclination = 0f,
            timestamp = System.currentTimeMillis(),
            isCalibrated = false
        )
    }

    private fun fuseFromAccelMag(
        accelValues: FloatArray,
        magValues: FloatArray
    ): CompassOrientation {
        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)

        val success = SensorManager.getRotationMatrix(rotationMatrix, null, accelValues, magValues)
        if (!success) {
            // getRotationMatrix returns false if device is in free fall or at magnetic pole
        }

        val remappedMatrix = remapMatrixBasedOnRotation(rotationMatrix)
        SensorManager.getOrientation(remappedMatrix, orientationAngles)

        return CompassOrientation(
            azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat().normalizeAzimuth(),
            trueAzimuth = null,
            pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat(),
            roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat(),
            accuracy = SensorAccuracy.UNRELIABLE,
            magneticDeclination = 0f,
            timestamp = System.currentTimeMillis(),
            isCalibrated = false
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun mapAccuracy(raw: Int?): SensorAccuracy = when (raw) {
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> SensorAccuracy.HIGH
        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> SensorAccuracy.MEDIUM
        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> SensorAccuracy.LOW
        else -> SensorAccuracy.UNRELIABLE
    }
}

private fun getScreenRotation(context: Context): Int {
    val displayManager = context.getSystemService<DisplayManager>()
    return displayManager?.getDisplay(Display.DEFAULT_DISPLAY)?.rotation ?: Surface.ROTATION_0
}
