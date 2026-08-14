package net.biblepassages.bp360view

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class PictureActivity : ComponentActivity() {

    private var imageSource: String? = null
    private var panoramaView: PanoramaView? = null
    private var loadingView: TextView? = null
    private var useMotionControl = false

    companion object {
        const val EXTRA_MEDIA_URL = "MEDIA_URL"
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

        imageSource = intent.getStringExtra(EXTRA_MEDIA_URL)

        if (imageSource == null) {
            showMessage("No picture selected.")
            return
        }

        showControlModeChoice()
    }

    override fun onResume() {
        super.onResume()
        panoramaView?.onResume()
    }

    override fun onPause() {
        panoramaView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        panoramaView = null
        loadingView = null
        super.onDestroy()
    }

    private fun showControlModeChoice() {
        setContentView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(44, 44, 44, 44)
                setBackgroundColor(AppStyle.background)

                addView(
                    TextView(this@PictureActivity).apply {
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
                    startPanorama()
                })

                addView(controlModeButton("Motion Control") {
                    useMotionControl = true
                    startPanorama()
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

    private fun startPanorama() {
        val selectedImageSource = imageSource ?: run {
            showMessage("No picture selected.")
            return
        }

        val root = FrameLayout(this).apply {
            setBackgroundColor(AppStyle.background)
        }

        panoramaView = PanoramaView(
            context = this,
            imagePath = selectedImageSource,
            useMotionControl = useMotionControl,
            onReady = { showLoading(false) }
        )

        root.addView(
            panoramaView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        loadingView = TextView(this).apply {
            text = "Loading..."
            textSize = 24f
            typeface = AppStyle.titleFont
            setTextColor(AppStyle.white)
            setBackgroundColor(AppStyle.background)
            gravity = Gravity.CENTER
        }

        root.addView(
            loadingView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)
        showLoading(true)
    }

    private fun showLoading(isLoading: Boolean) {
        loadingView?.visibility = if (isLoading) View.VISIBLE else View.GONE
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
}
