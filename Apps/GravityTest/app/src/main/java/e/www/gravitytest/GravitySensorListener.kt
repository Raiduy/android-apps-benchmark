package e.www.gravitytest

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class GravitySensorListener(context: Context) :
    SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var gsensor: Sensor

    init {
        sensorManager = context
            .getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    }

    fun start() {
        sensorManager.registerListener(
            this, gsensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }
}