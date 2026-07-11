package dev.jekis.canvacompass.domain

import kotlinx.coroutines.flow.Flow

interface SensorRepository {

    fun fetchOrientationData(): Flow<CompassOrientation>
}