package net.biblepassages.bp360view

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = ScrollView(this).apply {
            setBackgroundColor(AppStyle.background)
            isFillViewport = true
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            AppStyle.applySystemBarPadding(this, 0, 18, 0, 30)
        }

        AppStyle.addTopBanner(layout)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(42, 0, 42, 0)
        }

        val subtitle = TextView(this).apply {
            text = "360° Photos & Videos"
            textSize = 20f
            typeface = AppStyle.monoFont
            setTextColor(AppStyle.subtext)
            gravity = Gravity.CENTER
            setPadding(0, 6, 0, 24)
        }

        content.addView(subtitle)

        content.addView(
            menuImage(R.drawable.video_gallery) {
                startActivity(
                    Intent(
                        this,
                        VideoListActivity::class.java
                    )
                )
            }
        )

        content.addView(
            menuImage(R.drawable.photo_gallery) {
                startActivity(
                    Intent(
                        this,
                        PictureListActivity::class.java
                    )
                )
            }
        )

        val aboutIcon = ImageView(this).apply {
            setImageResource(R.drawable.about_us_button)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            isClickable = true
            isFocusable = true

            setOnClickListener {
                startActivity(
                    Intent(
                        this@MainActivity,
                        AboutActivity::class.java
                    )
                )
            }
        }

        content.addView(
            aboutIcon,
            LinearLayout.LayoutParams(
                500,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 72
                bottomMargin = 12
            }
        )
        layout.addView(content)

        root.addView(layout)

        setContentView(root)
    }

    private fun menuImage(
        drawableId: Int,
        action: () -> Unit
    ): ImageView {

        return ImageView(this).apply {

            setImageResource(drawableId)

            adjustViewBounds = true

            scaleType = ImageView.ScaleType.FIT_CENTER

            background = getDrawable(R.drawable.rounded_button)

            clipToOutline = true

            isClickable = true

            isFocusable = true

            setOnClickListener {
                action()
            }

            layoutParams = LinearLayout.LayoutParams(
                500,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
                bottomMargin = 12
            }
        }
    }
}
