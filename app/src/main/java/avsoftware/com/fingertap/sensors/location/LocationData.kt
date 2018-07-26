package avsoftware.com.fingertap.sensors.location

import avsoftware.com.fingertap.sensors.Recordable

data class  LocationData(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f, val timestamp: Long = 0L) : Recordable {
    override fun getBytes() = "GPS Event x:$x, y:$y, z:$z, timestamp:$timestamp\n".toByteArray()
}