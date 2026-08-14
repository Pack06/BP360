package net.biblepassages.bp360view

import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.video.spherical.SphericalGLSurfaceView
import androidx.media3.ui.PlayerView

class VideoActivity : ComponentActivity() {

    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var loadingView: TextView? = null
    private var hasRetriedAtLowerQuality = false
    private var hasRetriedWithDash = false
    private var originalMediaUri: Uri? = null
    private var useMotionControl = true

    companion object {
        const val EXTRA_MEDIA_URL = "MEDIA_URL"
        private const val DEFAULT_360_BANDWIDTH_HINT_MBPS = "8"
        private const val MEDIUM_360_BANDWIDTH_HINT_MBPS = "5"
        private const val LOW_360_BANDWIDTH_HINT_MBPS = "0.7"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        originalMediaUri = intent.getStringExtra(EXTRA_MEDIA_URL)
            ?.let { Uri.parse(it) }

        val selectedMediaUri = originalMediaUri
        if (selectedMediaUri == null) {
            showMessage("No video selected.")
            return
        }

        showControlModeChoice()
    }

    override fun onStart() {
        super.onStart()
        playerView?.onResume()
        refreshVideoMotionControl()
    }

    override fun onStop() {
        playerView?.onPause()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        playerView?.onResume()
        refreshVideoMotionControl()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        playerView?.onPause()
        playerView?.onResume()
        refreshVideoMotionControl()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
        playerView = null
        loadingView = null
    }

    private fun showControlModeChoice() {
        setContentView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(44, 44, 44, 44)
                setBackgroundColor(AppStyle.background)

                addView(
                    TextView(this@VideoActivity).apply {
                        text = "Choose Control Mode"
                        textSize = 28f
                        typeface = AppStyle.titleFont
                        setTextColor(AppStyle.white)
                        gravity = Gravity.CENTER
                        setPadding(0, 0, 0, 36)
                    }
                )

                addView(controlModeButton("Touch Control") {
                    useMotionControl = false
                    startPlayback()
                })

                addView(controlModeButton("Motion Control") {
                    useMotionControl = true
                    startPlayback()
                })
            }
        )
    }

    private fun controlModeButton(text: String, action: () -> Unit): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 23f
            typeface = AppStyle.titleFont
            setTextColor(AppStyle.white)
            gravity = Gravity.CENTER
            setPadding(0, 28, 0, 28)
            setBackgroundColor(AppStyle.card)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 12, 0, 12)
            }
        }
    }

    private fun startPlayback() {
        val selectedMediaUri = originalMediaUri ?: run {
            showMessage("No video selected.")
            return
        }

        val isEmulator = isProbablyEmulator()
        val mediaUri = selectedMediaUri.withClientBandwidthHint(
            if (isEmulator) LOW_360_BANDWIDTH_HINT_MBPS else DEFAULT_360_BANDWIDTH_HINT_MBPS
        )

        setContentView(R.layout.activity_main)

        playerView = findViewById<PlayerView>(R.id.playerView).apply {
            keepScreenOn = true
            configureSphericalSurface(useMotionControl)
        }
        addLoadingOverlay()
        showLoading(true)

        val mediaItem = buildMediaItem(mediaUri, MimeTypes.APPLICATION_M3U8)

        val renderersFactory = DefaultRenderersFactory(this)
            .setEnableDecoderFallback(true)

        player = ExoPlayer.Builder(this, renderersFactory).build().also { exoPlayer ->
            playerView?.player = exoPlayer
            refreshVideoMotionControl()

            useCompatible360VideoTracks(exoPlayer, isEmulator)

            exoPlayer.addListener(
                object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        showLoading(
                            playbackState == Player.STATE_BUFFERING ||
                                    playbackState == Player.STATE_IDLE
                        )
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        if (
                            error.errorCode == PlaybackException.ERROR_CODE_DECODING_FAILED &&
                            !hasRetriedAtLowerQuality
                        ) {
                            hasRetriedAtLowerQuality = true
                            val retryHint = if (isEmulator) {
                                LOW_360_BANDWIDTH_HINT_MBPS
                            } else {
                                MEDIUM_360_BANDWIDTH_HINT_MBPS
                            }
                            restartPlayback(
                                exoPlayer = exoPlayer,
                                mediaItem = mediaItem.buildUpon()
                                    .setUri(selectedMediaUri.withClientBandwidthHint(retryHint))
                                    .build(),
                                useLowQualityTracks = true,
                                isEmulator = isEmulator
                            )
                        } else if (
                            error.errorCode == PlaybackException.ERROR_CODE_DECODING_FAILED &&
                            !hasRetriedWithDash
                        ) {
                            hasRetriedWithDash = true
                            restartPlayback(
                                exoPlayer = exoPlayer,
                                mediaItem = buildMediaItem(
                                    selectedMediaUri.toDashManifestUri()
                                        .withClientBandwidthHint(LOW_360_BANDWIDTH_HINT_MBPS),
                                    MimeTypes.APPLICATION_MPD
                                ),
                                useLowQualityTracks = true,
                                isEmulator = isEmulator
                            )
                        } else {
                            showPlaybackError(error)
                        }
                    }
                }
            )

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    private fun showMessage(message: String) {
        setContentView(
            TextView(this).apply {
                text = message
                textSize = 20f
                typeface = AppStyle.titleFont
                setTextColor(AppStyle.white)
                setBackgroundColor(AppStyle.background)
                gravity = Gravity.CENTER
            }
        )
    }

    private fun isProbablyEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic", ignoreCase = true) ||
                Build.FINGERPRINT.contains("emulator", ignoreCase = true) ||
                Build.MODEL.contains("sdk", ignoreCase = true) ||
                Build.MODEL.contains("emulator", ignoreCase = true) ||
                Build.PRODUCT.contains("sdk", ignoreCase = true) ||
                Build.HARDWARE.contains("goldfish", ignoreCase = true) ||
                Build.HARDWARE.contains("ranchu", ignoreCase = true)
    }

    private fun addLoadingOverlay() {
        loadingView = TextView(this).apply {
            text = "Loading..."
            textSize = 24f
            typeface = AppStyle.titleFont
            setTextColor(AppStyle.white)
            setBackgroundColor(AppStyle.background)
            gravity = Gravity.CENTER
        }

        addContentView(
            loadingView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun showLoading(isLoading: Boolean) {
        loadingView?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun refreshVideoMotionControl() {
        playerView?.let { view ->
            view.post {
                view.configureSphericalSurface(useMotionControl)
            }
            view.postDelayed(
                {
                    view.configureSphericalSurface(useMotionControl)
                },
                300L
            )
            view.postDelayed(
                {
                    view.configureSphericalSurface(useMotionControl)
                },
                1000L
            )
        }
    }

    private fun PlayerView.configureSphericalSurface(enableMotionControl: Boolean) {
        val sphericalSurface = (videoSurfaceView as? SphericalGLSurfaceView)
            ?: findSphericalSurface(this)

        sphericalSurface?.apply {
            setDefaultStereoMode(C.STEREO_MODE_MONO)
            setUseSensorRotation(enableMotionControl)
            if (enableMotionControl) {
                onResume()
            }
        }
    }

    private fun findSphericalSurface(view: View): SphericalGLSurfaceView? {
        if (view is SphericalGLSurfaceView) {
            return view
        }

        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                val found = findSphericalSurface(view.getChildAt(index))
                if (found != null) {
                    return found
                }
            }
        }

        return null
    }

    private fun buildMediaItem(uri: Uri, mimeType: String): MediaItem {
        return MediaItem.Builder()
            .setUri(uri)
            .setMimeType(mimeType)
            .build()
    }

    private fun useCompatible360VideoTracks(exoPlayer: ExoPlayer, isEmulator: Boolean) {
        val maxWidth = if (isEmulator) 480 else 2160
        val maxHeight = if (isEmulator) 240 else 1080
        val maxBitrate = if (isEmulator) 900_000 else 11_000_000

        val parameters = TrackSelectionParameters.Builder()
            .setMaxVideoSize(maxWidth, maxHeight)
            .setMaxVideoBitrate(maxBitrate)
            .setForceLowestBitrate(isEmulator)
            .build()

        exoPlayer.setTrackSelectionParameters(parameters)
    }

    private fun restartPlayback(
        exoPlayer: ExoPlayer,
        mediaItem: MediaItem,
        useLowQualityTracks: Boolean,
        isEmulator: Boolean
    ) {
        if (useLowQualityTracks) {
            useCompatible360VideoTracks(exoPlayer, isEmulator)
        }
        showLoading(true)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    private fun Uri.withClientBandwidthHint(hintMbps: String): Uri {
        return buildUpon()
            .appendQueryParameter("clientBandwidthHint", hintMbps)
            .build()
    }

    private fun Uri.toDashManifestUri(): Uri {
        return buildUpon()
            .path(path.orEmpty().replace("manifest/video.m3u8", "manifest/video.mpd"))
            .clearQuery()
            .build()
    }

    private fun showPlaybackError(error: PlaybackException) {
        releasePlayer()
        playerView = null

        showMessage(
            "Video could not play in 3D mode.\n" +
                    error.errorCodeName
        )
    }

    private fun releasePlayer() {
        playerView?.player = null
        player?.release()
        player = null
    }
}
