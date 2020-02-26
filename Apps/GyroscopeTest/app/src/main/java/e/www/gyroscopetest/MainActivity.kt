package e.www.gyroscopetest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    // Sensor listener variable
    private lateinit var sensorListener: GyroscopeSensorListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the sensor listener
        sensorListener = GyroscopeSensorListener(this)
    }

    override fun onStart() {
        // Start the sensor listener
        sensorListener.start()
        super.onStart()
    }

    override fun onStop() {
        // Stop the sensor listener
        sensorListener.stop()
        super.onStop()
    }
}
