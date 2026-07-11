package dev.jekis.canvacompass.data.datasource.sensors

import android.hardware.Sensor
import android.hardware.SensorManager

/**
 * Реализован датчик магнитного поля (магнитометр).
 * Измеряет напряженность окружающего магнитного поля по трем осям.
 * Используется для определения ориентации устройства относительно северного магнитного полюса.
 *
 * @param SensorManager - системный сервис для работы с датчиками Android.
 */
class AccelerometerSensor(sensorManager: SensorManager) :
    AbstractSensor(sensorManager, Sensor.TYPE_ACCELEROMETER)