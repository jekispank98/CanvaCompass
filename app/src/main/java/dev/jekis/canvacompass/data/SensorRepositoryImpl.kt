package dev.jekis.canvacompass.data

import android.content.Context
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.Surface
import android.view.WindowManager
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
    private val context: Context
) : SensorRepository {
    @OptIn(FlowPreview::class)
    override fun fetchOrientationData(): Flow<CompassOrientation> {

        val accelerometer = AccelerometerSensor(sensorManager)
        val magnetometer = MagnetometerSensor(sensorManager)
        val rotationVector = RotationVectorSensor(sensorManager)

        val orientationFlow = if (rotationVector.isAvailable()) {
            rotationVector.sensorState
                .filter { it.values.isNotEmpty() }
                .conflate()
                .map { fuseFromRotationVector(it.values) }
        } else {
            combine(
                accelerometer.sensorState.filter { it.values.isNotEmpty() },
                magnetometer.sensorState.filter { it.values.isNotEmpty() }
            ) { accel, mag -> fuseFromAccelMag(accel.values, mag.values) }
                .conflate()
        }

        return combine(
            orientationFlow,
            magnetometer.sensorState
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

    private fun getScreenRotation(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            displayManager.getDisplay(Display.DEFAULT_DISPLAY)?.rotation ?: Surface.ROTATION_0
        } else {
            @Suppress("DEPRECATION")
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.rotation
        }
    }

    private fun remapMatrixBasedOnRotation(rotationMatrix: FloatArray): FloatArray {
        val remappedMatrix = FloatArray(9)
        val worldAxisForDeviceAxisX: Int
        val worldAxisForDeviceAxisY: Int

        when (getScreenRotation()) {
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
            else -> { // Surface.ROTATION_0
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
            // getRotationMatrix возвращает false, если устройство в свободном падении
            // или в near-magnetic-pole сингулярности — нужно решить, что возвращать в этом случае
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

    private fun mapAccuracy(raw: Int?): SensorAccuracy = when (raw) {
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> SensorAccuracy.HIGH
        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> SensorAccuracy.MEDIUM
        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> SensorAccuracy.LOW
        else -> SensorAccuracy.UNRELIABLE
    }

}