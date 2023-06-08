package x.intervalapp.cameratest

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock.sleep
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


private const val RUN_INTERVAL: Long = 5000 // 5 seconds
private const val IDLE_INTERVAL: Long = 5000 // 5 seconds

private lateinit var imageReader: ImageReader
private var cameraId: String? = null
private var cameraDevice: CameraDevice? = null
private var mSession: CameraCaptureSession? = null
private var cameraManager: CameraManager? = null
private var cameraCharacteristics: CameraCharacteristics? = null
private var biggestSize: Size? = null
private var targets: MutableList<Surface>? = null
private var runnable: Runnable by Delegates.notNull()

// Handler for infinite loop
private lateinit var handler: Handler

// Callback to assign cameraDevice
private val mStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
    override fun onOpened(camera: CameraDevice) {
        cameraDevice = camera
        cameraDevice?.createCaptureSession(targets!!, mccsStateCallback, null)
    }

    override fun onDisconnected(camera: CameraDevice) {
        cameraDevice?.close()
    }

    override fun onError(camera: CameraDevice, error: Int) {
        cameraDevice?.close()
        cameraDevice = null
    }
}

private val mccsStateCallback: CameraCaptureSession.StateCallback =
    object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            try {
                mSession = session
                val request =
                    cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                request.addTarget(targets!![0])

                handler = Handler()

                // Runnable which takes a picture
                runnable = Runnable {
                    // Take a picture
                    val now = System.currentTimeMillis()

                    while (System.currentTimeMillis() - now < RUN_INTERVAL) {
                        Log.i("MainActivity", "take picture + ${System.currentTimeMillis()}")
                        mSession?.capture(
                            request.build(),
                            object : CameraCaptureSession.CaptureCallback() {
                                override fun onCaptureCompleted(
                                    session: CameraCaptureSession,
                                    request: CaptureRequest,
                                    result: TotalCaptureResult
                                ) {
                                    super.onCaptureCompleted(session, request, result)
                                }
                            },
                            null
                        )
                    }
                    handler.postDelayed(runnable, IDLE_INTERVAL)
                }

                // Start infinite loop
                handler.post(runnable)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {}
    }

class MainActivity : ComponentActivity() {
    private var runner: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        initialiseCamera()

//        while (true) {
//            runner = CoroutineScope(Dispatchers.IO).launch {
//                doThings()
//                runner = null
//            }
            initialiseCamera()
//            sleep(10000)
//            runner?.cancel()
            Log.i("MainActivity", "cancel + ${System.currentTimeMillis()}")
//            sleep(IDLE_INTERVAL)
//        }
    }

    private fun initialiseCamera() {
        Log.i("MainActivity", "initialiseCamera")
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = getFrontFacingCameraId(cameraManager)
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
                cameraCharacteristics = cameraManager?.getCameraCharacteristics(cameraId!!)
                val streamConfigurationMap: StreamConfigurationMap? =
                    cameraCharacteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val jpegSizes =
                    streamConfigurationMap!!.getOutputSizes(ImageFormat.JPEG)
                biggestSize = Size(0, 0)
                for (size in jpegSizes) {
                    if (size.height >= biggestSize!!.height && size.width >= biggestSize!!.width) {
                        biggestSize = size
                    }
                }
                imageReader =
                    ImageReader.newInstance(biggestSize!!.width, biggestSize!!.height, ImageFormat.PRIVATE, 50)
                val imReaderSurface = imageReader.surface
                targets = arrayOf(imReaderSurface).toMutableList()
                cameraManager?.openCamera(cameraId!!, mStateCallback, null)
                Log.i("Camera permission", "Granted")
            } else {
                Log.d("Camera permission", "Missing")
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private fun getFrontFacingCameraId(cameraManager: CameraManager?): String? {
        try {
            Log.i("MainActivity", "getFrontFacingCameraId")
            for (id in cameraManager!!.cameraIdList) {
                val cameraCharacteristics =
                    cameraManager.getCameraCharacteristics(id)
                val cameraOrientation =
                    cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                if (cameraOrientation != null && cameraOrientation == CameraMetadata.LENS_FACING_FRONT) {
                    Log.i("MainActivity", "getFrontFacingCameraId + $id")
                    return id
                }
            }
        } catch (ex: CameraAccessException) {
            ex.printStackTrace()
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        runner?.cancel()
    }

}