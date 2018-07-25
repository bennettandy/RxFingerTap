package avsoftware.com.fingertap.sensors

data class  AccelerometerData( val x: Float, val y: Float, val z: Float, val timestamp: Long ) : Recordable {
    override fun getBytes() = "Event x:$x, y:$y, z:$z, timestamp:$timestamp\n".toByteArray()
}