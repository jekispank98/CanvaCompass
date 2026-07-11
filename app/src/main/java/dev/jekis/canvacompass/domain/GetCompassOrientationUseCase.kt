package dev.jekis.canvacompass.domain

import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetCompassOrientationUseCase(
    private val sensorRepository: SensorRepository
) {
    operator fun invoke(): Flow<CompassOrientation> = sensorRepository.fetchOrientationData()
}