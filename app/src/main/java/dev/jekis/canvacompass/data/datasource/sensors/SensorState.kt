package dev.jekis.canvacompass.data.datasource.sensors

data class SensorState(
    val name: String?,
    val values: FloatArray = floatArrayOf(),
    val accuracy: Int? = null,
    val timestamp: Long? = 0L
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorState

        if (accuracy != other.accuracy) return false
        if (timestamp != other.timestamp) return false
        if (name != other.name) return false
        if (!values.contentEquals(other.values)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = accuracy ?: 0
        result = 31 * result + (timestamp?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + values.contentHashCode()
        return result
    }

}
