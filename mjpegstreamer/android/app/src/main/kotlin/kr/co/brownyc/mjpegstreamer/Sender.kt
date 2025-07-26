package kr.co.brownyc.mjpegstreamer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import okhttp3.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

object Sender {
    private const val TAG = "Sender"
    private var websocketUrl: String = "ws://localhost:8090/ws"
    private var jpegQuality = 60
    private var targetWidth = 640
    private var targetHeight = 480

    private var cameraProvider: ProcessCameraProvider? = null
    private var analysis: ImageAnalysis? = null
    private var audioRecord: AudioRecord? = null
    private var audioThread: Thread? = null
    private var webSocket: WebSocket? = null
    @Volatile private var streaming = false
    private val uid = java.util.UUID.randomUUID().toString()

    var isStreaming = false

    fun start(context: Context, wsUrl: String, width: Int, height: Int, quality: Int) {
        if (streaming) return
        websocketUrl = wsUrl
        jpegQuality = quality
        targetWidth = width
        targetHeight = height
        setupWebSocket()
        startCamera(context)
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED)
        startAudio()
        streaming = true
        isStreaming = streaming
    }

    fun stop() {
        if (!streaming) return
        audioThread?.interrupt()
        streaming = false
        isStreaming = streaming
        cameraProvider?.unbindAll()
        audioRecord?.stop()
        webSocket?.close(1000, "Bye")
    }

    private fun setupWebSocket() {
        val client = OkHttpClient()
        val req = Request.Builder().url(websocketUrl).build()
        Log.d("Sender", "setupWebSocket $websocketUrl")
        client.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, resp: Response) {
                webSocket = ws
//                Log.d(TAG, "WS connected")
                Log.d("Sender", "Connect to $websocketUrl")
            }
            override fun onFailure(ws: WebSocket, t: Throwable, resp: Response?) {
                Log.e(TAG, "WS error: ${t.message}")
            }
        })
    }

    private fun startCamera(context: Context) {
        val camProviderFuture = ProcessCameraProvider.getInstance(context)
        camProviderFuture.addListener({
            cameraProvider = camProviderFuture.get()
            val preview = Preview.Builder().build()
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { image ->
                if (streaming && webSocket != null) {
                    val bmp = image.toBitmap() // defined below
                    val baos = ByteArrayOutputStream()
                    val resized = Bitmap.createScaledBitmap(bmp, targetWidth, targetHeight, true)
//                    bmp.compress(Bitmap.CompressFormat.JPEG, 60, baos)
                    resized.compress(Bitmap.CompressFormat.JPEG, jpegQuality, baos)

                    val b64 = "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                    val jobj = JSONObject().apply {
                        put("type", "video")
                        put("uid", uid); put("timestamp", System.currentTimeMillis())
                        put("frame", b64)
                    }
                    webSocket?.send(jobj.toString())
                }
                image.close()
            }
            cameraProvider?.bindToLifecycle(/* lifecycleOwner= */context as androidx.lifecycle.LifecycleOwner,
                androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA,
                preview, analysis)
            this.analysis = analysis
        }, ContextCompat.getMainExecutor(context))
    }



    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startAudio() {
        val bufSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufSize)
        audioRecord?.startRecording()
        audioThread = Thread {
            val buffer = ByteArray(bufSize)
            while (streaming && audioRecord != null) {
                val read = audioRecord!!.read(buffer, 0, buffer.size)
                if (read > 0 && webSocket != null) {
                    val b64 = Base64.encodeToString(buffer.copyOf(read), Base64.NO_WRAP)
                    val jobj = JSONObject().apply {
                        put("type", "audio")
                        put("uid", uid); put("timestamp", System.currentTimeMillis())
                        put("data", b64)
                    }
                    webSocket?.send(jobj.toString())
                }
            }
        }
        audioThread?.start()
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val plane = planes.first()
        val buffer = plane.buffer
        val bytes = ByteArray(buffer.remaining()).also { buffer.get(it) }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}