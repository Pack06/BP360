package net.biblepassages.bp360view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.ComponentActivity

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(AppStyle.background)
            AppStyle.applySystemBarPadding(this, 0, 18, 0, 30)
        }

        layout.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.splash_logo)
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER
            },
            LinearLayout.LayoutParams(
                260,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        setContentView(layout)

        Handler(Looper.getMainLooper()).postDelayed(
            {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            },
            800L
        )
    }
}
