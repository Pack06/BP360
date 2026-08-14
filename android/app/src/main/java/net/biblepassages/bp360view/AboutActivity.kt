package net.biblepassages.bp360view

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity

class AboutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)

        val background = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        repeat(2) {
            background.addView(
                ImageView(this).apply {
                    setImageResource(R.drawable.about_background)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            )
        }

        root.addView(
            background,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            View(this).apply {
                setBackgroundColor(AppStyle.background)
                alpha = 0.62f
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            AppStyle.applySystemBarPadding(this, 0, 18, 0, 60)
        }

        AppStyle.addTopBanner(layout)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 0, 50, 0)
        }

        val title = TextView(this).apply {
            text = "BP360View"
            textSize = 36f
            typeface = AppStyle.titleFont
            setTextColor(AppStyle.white)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "360° Media Viewer"
            textSize = 20f
            typeface = AppStyle.monoFont
            setTextColor(AppStyle.subtext)
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 40)
        }

        val body = TextView(this).apply {
            text =
                "One of the aims of Bible Passages is to produce high-quality, immersive 360° photographs and videos designed to transport viewers into the heart of the biblical narrative, bringing the stories of Scripture—and the actual historical locations where those events unfolded—vividly to life.\n\n" +
                        "Our main mission is the same as our Lord’s: making disciples who grow more like Christ and glorify God. We do this by teaching the Bible, its story, and the places where it unfolded, serving as a resource for every Christian who wants to go deeper in their understanding of Scripture.\n\n" +
                        "Bible Passages is a 501(c)(3) nonprofit ministry made possible through the generous support of our donors and ministry partners.\n\n" +
                        "For more information, visit biblepassages.net."
            textSize = 18f
            typeface = AppStyle.monoFont
            setTextColor(AppStyle.white)
            gravity = Gravity.CENTER
            setLineSpacing(8f, 1.0f)
        }

        val back = TextView(this).apply {
            text = "← Back"
            textSize = 22f
            typeface = AppStyle.titleFont
            setTextColor(AppStyle.white)
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
            setBackgroundColor(AppStyle.card)
            setOnClickListener { finish() }
        }

        content.addView(title)
        content.addView(subtitle)
        content.addView(body)

        layout.addView(
            ScrollView(this).apply {
                isFillViewport = true
                addView(content)
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        layout.addView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 0, 50, 0)
                addView(back)
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            layout,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)
    }
}
