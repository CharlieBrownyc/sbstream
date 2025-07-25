package kr.co.brownyc.mjpegstreamer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    companion object {
        private const val CHANNEL = "kr.co.brownyc.mjpegstreamer/camera"
        private const val PERMISSION_REQUEST = 1001
    }
    private var isPermissionsGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")
        requestPermissionsIfNeeded()
    }

    private fun requestPermissionsIfNeeded() {
        val needed = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        val missing = needed.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), PERMISSION_REQUEST)
        } else {
            isPermissionsGranted = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, results: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (requestCode == PERMISSION_REQUEST) {
            isPermissionsGranted = results.all { it == PackageManager.PERMISSION_GRANTED }
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            if (!isPermissionsGranted) {
                result.error("PERM", "Permissions not granted", null)
                return@setMethodCallHandler
            }
            when (call.method) {
                "startCamera" -> {
                    val wsUrl = call.argument<String>("ws_url") ?: ""
                    val width = call.argument<Int>("width") ?: 640
                    val height = call.argument<Int>("height") ?: 480
                    val jpegQ = call.argument<Int>("jpeg_quality") ?: 60

                    Log.d("FlutterBridge", "StartCamera invoked")
                    Log.d("FlutterBridge", "wsUrl=$wsUrl, width=$width, height=$height, quality=$jpegQ")

                    Sender.start(this, wsUrl, width, height, jpegQ)
                    result.success(null)
                }
                "stopCamera" -> {
                    Sender.stop()
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }
    }

}
