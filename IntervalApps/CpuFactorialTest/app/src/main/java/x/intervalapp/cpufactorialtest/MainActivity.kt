package x.intervalapp.cpufactorialtest

import android.os.Bundle
import android.os.SystemClock.sleep
import android.util.Log
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private var runner: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        while (true) {
            runner = CoroutineScope(Dispatchers.IO).launch {
                doThings()
                runner = null
            }
            sleep(RUN_INTERVAL)
            runner?.cancel()
//            Log.i("MainActivity", "cancel + ${System.currentTimeMillis()}")
            sleep(IDLE_INTERVAL)
        }
    }

    private fun doThings() {
//        Log.i("MainActivity", "launch + ${System.currentTimeMillis()}")
        while (true) {
            var res = 1
            for (i in 1..256) {
                res *= i
            }
        }
    }

    companion object {
        private const val RUN_INTERVAL: Long = 5000 // 5 seconds
        private const val IDLE_INTERVAL: Long = 5000 // 5 seconds
    }
}
