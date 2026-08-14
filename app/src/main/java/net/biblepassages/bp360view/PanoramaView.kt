package net.biblepassages.bp360view

import android.content.Context
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.Surface
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PanoramaView(
    context: Context,
    private val imagePath: String,
    private val useMotionControl: Boolean = false,
    private val onReady: () -> Unit = {}
) : GLSurfaceView(context), SensorEventListener {

    private val renderer: PanoramaRenderer
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private var previousX = 0f
    private var previousY = 0f
    private var previousDistance = 0f
    private var initialYaw: Float? = null
    private var initialPitch: Float? = null

    init {
        setEGLContextClientVersion(2)
        renderer = PanoramaRenderer(context, imagePath, onReady)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.pointerCount) {
            1 -> {
                val x = event.x
                val y = event.y

                if (!useMotionControl && event.action == MotionEvent.ACTION_MOVE) {
                    val dx = x - previousX
                    val dy = y - previousY
                    renderer.addTouchRotation(dx, dy)
                }

                previousX = x
                previousY = y
                previousDistance = 0f
            }

            2 -> {
                val dx = event.getX(0) - event.getX(1)
                val dy = event.getY(0) - event.getY(1)
                val distance = sqrt(dx * dx + dy * dy)

                if (previousDistance > 0f && event.action == MotionEvent.ACTION_MOVE) {
                    val distanceChange = distance - previousDistance
                    renderer.addZoom(distanceChange)
                }

                previousDistance = distance
            }
        }

        return true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        registerMotionSensor()
    }

    override fun onDetachedFromWindow() {
        sensorManager.unregisterListener(this)
        super.onDetachedFromWindow()
    }

    override fun onResume() {
        super.onResume()
        registerMotionSensor()
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!useMotionControl || event.sensor != rotationSensor) {
            return
        }

        val rotationMatrix = FloatArray(9)
        val displayAdjustedMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        remapForDisplayRotation(rotationMatrix, displayAdjustedMatrix)
        SensorManager.getOrientation(displayAdjustedMatrix, orientation)

        val currentYaw = Math.toDegrees(orientation[0].toDouble()).toFloat()
        val currentPitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
        val baselineYaw = initialYaw ?: currentYaw.also { initialYaw = it }
        val baselinePitch = initialPitch ?: currentPitch.also { initialPitch = it }

        renderer.setMotionRotation(
            yawDegrees = normalizeDegrees(baselineYaw - currentYaw),
            pitchDegrees = (currentPitch - baselinePitch).coerceIn(-89f, 89f)
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun registerMotionSensor() {
        if (!useMotionControl || rotationSensor == null) {
            return
        }

        sensorManager.unregisterListener(this)
        sensorManager.registerListener(
            this,
            rotationSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    private fun remapForDisplayRotation(
        inputMatrix: FloatArray,
        outputMatrix: FloatArray
    ) {
        when (display?.rotation ?: Surface.ROTATION_0) {
            Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
                inputMatrix,
                SensorManager.AXIS_Z,
                SensorManager.AXIS_MINUS_X,
                outputMatrix
            )

            Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
                inputMatrix,
                SensorManager.AXIS_MINUS_X,
                SensorManager.AXIS_MINUS_Z,
                outputMatrix
            )

            Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
                inputMatrix,
                SensorManager.AXIS_MINUS_Z,
                SensorManager.AXIS_X,
                outputMatrix
            )

            else -> SensorManager.remapCoordinateSystem(
                inputMatrix,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Z,
                outputMatrix
            )
        }
    }

    private fun normalizeDegrees(degrees: Float): Float {
        var normalized = degrees
        while (normalized > 180f) normalized -= 360f
        while (normalized < -180f) normalized += 360f
        return normalized
    }
}

class PanoramaRenderer(
    private val context: Context,
    private val imagePath: String,
    private val onReady: () -> Unit
) : GLSurfaceView.Renderer {

    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val mvp = FloatArray(16)

    private var width = 1
    private var height = 1

    private var program = 0
    private var textureId = 0

    private lateinit var vertexBuffer: java.nio.FloatBuffer
    private lateinit var texBuffer: java.nio.FloatBuffer
    private var vertexCount = 0

    @Volatile
    private var yaw = 0f

    @Volatile
    private var pitch = 0f

    private var fieldOfView = 90f

    fun addTouchRotation(dx: Float, dy: Float) {
        yaw += dx * 0.2f
        pitch += dy * 0.2f

        if (pitch > 89f) pitch = 89f
        if (pitch < -89f) pitch = -89f
    }

    fun addZoom(distanceChange: Float) {
        fieldOfView -= distanceChange * 0.05f

        if (fieldOfView < 30f) fieldOfView = 30f
        if (fieldOfView > 120f) fieldOfView = 120f

        updateProjection()
    }

    fun setMotionRotation(yawDegrees: Float, pitchDegrees: Float) {
        yaw = yawDegrees
        pitch = (-pitchDegrees).coerceIn(-89f, 89f)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        program = createProgram()
        textureId = loadTexture()

        createSphere()

        Handler(Looper.getMainLooper()).post {
            onReady()
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.width = width
        this.height = height

        GLES20.glViewport(0, 0, width, height)
        updateProjection()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glUseProgram(program)

        Matrix.setIdentityM(view, 0)
        Matrix.rotateM(view, 0, -pitch, 1f, 0f, 0f)
        Matrix.rotateM(view, 0, -yaw, 0f, 1f, 0f)
        Matrix.multiplyMM(mvp, 0, projection, 0, view, 0)

        val positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        val texHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        val mvpHandle = GLES20.glGetUniformLocation(program, "uMVP")
        val textureHandle = GLES20.glGetUniformLocation(program, "uTexture")

        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvp, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureHandle, 0)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glEnableVertexAttribArray(texHandle)
        GLES20.glVertexAttribPointer(texHandle, 2, GLES20.GL_FLOAT, false, 0, texBuffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texHandle)
    }

    private fun updateProjection() {
        Matrix.perspectiveM(
            projection,
            0,
            fieldOfView,
            width.toFloat() / height.toFloat(),
            0.1f,
            100f
        )
    }

    private fun createSphere() {
        val vertices = mutableListOf<Float>()
        val texCoords = mutableListOf<Float>()

        val stacks = 40
        val slices = 80
        val radius = 10f

        for (i in 0 until stacks) {
            val lat0 = PI * (-0.5 + i.toDouble() / stacks)
            val lat1 = PI * (-0.5 + (i + 1).toDouble() / stacks)

            for (j in 0 until slices) {
                val lon0 = 2 * PI * j.toDouble() / slices
                val lon1 = 2 * PI * (j + 1).toDouble() / slices

                addQuad(vertices, texCoords, radius, lat0, lat1, lon0, lon1)
            }
        }

        vertexCount = vertices.size / 3

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices.toFloatArray())
                position(0)
            }

        texBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(texCoords.toFloatArray())
                position(0)
            }
    }

    private fun addQuad(
        vertices: MutableList<Float>,
        tex: MutableList<Float>,
        r: Float,
        lat0: Double,
        lat1: Double,
        lon0: Double,
        lon1: Double
    ) {
        val points = listOf(
            Pair(lat0, lon0),
            Pair(lat1, lon0),
            Pair(lat1, lon1),
            Pair(lat0, lon0),
            Pair(lat1, lon1),
            Pair(lat0, lon1)
        )

        for ((lat, lon) in points) {
            val x = (r * cos(lat) * sin(lon)).toFloat()
            val y = (r * sin(lat)).toFloat()
            val z = (r * cos(lat) * cos(lon)).toFloat()

            vertices.add(-x)
            vertices.add(y)
            vertices.add(z)

            tex.add((lon / (2 * PI)).toFloat())
            tex.add((0.5 - lat / PI).toFloat())
        }
    }

    private fun loadTexture(): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        val inputStream = if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            URL(imagePath).openStream()
        } else {
            context.assets.open(imagePath)
        }

        val bitmap = inputStream.use { BitmapFactory.decodeStream(it) }

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        bitmap.recycle()

        return textureIds[0]
    }

    private fun createProgram(): Int {
        val vertexShaderCode = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            uniform mat4 uMVP;
            varying vec2 vTexCoord;

            void main() {
                gl_Position = uMVP * aPosition;
                vTexCoord = aTexCoord;
            }
        """

        val fragmentShaderCode = """
            precision mediump float;
            uniform sampler2D uTexture;
            varying vec2 vTexCoord;

            void main() {
                gl_FragColor = texture2D(uTexture, vTexCoord);
            }
        """

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        return GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    private fun loadShader(type: Int, code: String): Int {
        return GLES20.glCreateShader(type).also {
            GLES20.glShaderSource(it, code)
            GLES20.glCompileShader(it)
        }
    }
}
