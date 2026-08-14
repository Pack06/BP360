package net.biblepassages.bp360view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object AppStyle {
    val background = Color.parseColor("#05242F")
    val card = Color.parseColor("#0A3645")
    val white = Color.WHITE
    val subtext = Color.LTGRAY

    val titleFont: Typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    val monoFont: Typeface = Typeface.create("monospace", Typeface.NORMAL)

    fun topBanner(context: Context): ImageView {
        return ImageView(context).apply {
            setImageResource(R.drawable.home_banner)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

    fun addTopBanner(layout: LinearLayout, bottomMargin: Int = 18) {
        layout.addView(
            topBanner(layout.context),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                this.bottomMargin = bottomMargin
            }
        )
    }

    fun applySystemBarPadding(
        view: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { target, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            target.setPadding(
                left + systemBars.left,
                top + systemBars.top,
                right + systemBars.right,
                bottom + systemBars.bottom
            )
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }
}
