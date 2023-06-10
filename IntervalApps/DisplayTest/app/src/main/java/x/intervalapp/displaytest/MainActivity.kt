package x.intervalapp.displaytest

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.VideoView
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {
    private lateinit var videoView : VideoView
    private lateinit var videoUri: Uri

    private val handler = Handler(Looper.getMainLooper())
    private val playRunnable = Runnable { playVideo() }
    private val stopRunnable = Runnable { stopVideo() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up videoview
        videoView = findViewById(R.id.videoView)
        videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.videofile)
        videoView.setOnPreparedListener { mp -> mp.setVolume(0f, 0f) }

        playVideo()
    }

    private fun playVideo() {
        videoView.setVideoURI(videoUri)
        videoView.start()
        handler.postDelayed(stopRunnable, RUN_INTERVAL)  // Stop video after RUN_INTERVAL
    }

    private fun stopVideo() {
        videoView.stopPlayback()
        handler.postDelayed(playRunnable, IDLE_INTERVAL) // Play video after IDLE_INTERVAL
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val RUN_INTERVAL : Long = 5000
        private const val IDLE_INTERVAL : Long = 5000
    }
}
