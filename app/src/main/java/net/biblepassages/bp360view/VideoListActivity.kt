package net.biblepassages.bp360view

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

class VideoListActivity : ComponentActivity() {

    private val categories = listOf(
        "Walking Videos",
        "Conv Videos",
        "PTP Videos"
    )

    private lateinit var header: TextView
    private lateinit var listContainer: LinearLayout
    private lateinit var footerButton: TextView
    private var catalog: CloudMediaCatalog? = null
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this).apply {
            setBackgroundColor(AppStyle.background)
        }

        root.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.videos_background)
                scaleType = ImageView.ScaleType.CENTER_CROP
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            View(this).apply {
                setBackgroundColor(AppStyle.background)
                alpha = 0.55f
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val scroll = ScrollView(this).apply {
            isFillViewport = true
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            AppStyle.applySystemBarPadding(this, 0, 18, 0, 40)
        }

        AppStyle.addTopBanner(layout)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 0, 40, 0)
        }

        header = TextView(this).apply {
            text = "Videos"
            textSize = 32f
            typeface = AppStyle.titleFont
            setTextColor(AppStyle.white)
            setPadding(0, 0, 0, 30)
        }

        content.addView(header)

        listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        content.addView(listContainer)

        val spacer = LinearLayout(this)
        content.addView(
            spacer,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        footerButton = backButton()
        content.addView(footerButton)

        layout.addView(content)

        scroll.addView(layout)
        root.addView(
            scroll,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        setContentView(root)

        showMessage("Loading videos...")

        CloudMediaCatalogLoader.load(
            onLoaded = { loadedCatalog ->
                catalog = loadedCatalog
                showCategories()
            }
        )

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (selectedCategory != null) {
                        showCategories()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    private fun mediaRow(icon: String, titleText: String, action: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(28, 24, 28, 24)
            setBackgroundColor(AppStyle.card)
            isClickable = true
            isFocusable = true

            val text = TextView(this@VideoListActivity).apply {
                text = if (icon.isBlank()) titleText else "$icon  $titleText"
                textSize = 21f
                typeface = AppStyle.titleFont
                setTextColor(AppStyle.white)
            }

            addView(text)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 10, 0, 10)
            layoutParams = params

            setOnClickListener { action() }
        }
    }

    private fun walkingVideoButton(titleText: String, action: () -> Unit): FrameLayout {
        return imageButton(
            drawableId = R.drawable.walking_video_button_template,
            titleText = titleText,
            action = action,
            overlayTitle = walkingButtonTitle(titleText),
            overlayTextSize = walkingButtonTextSize(titleText),
            overlayHeight = 112,
            overlayBottomMargin = 42
        )
    }

    private fun imageButton(
        drawableId: Int,
        titleText: String,
        action: () -> Unit,
        overlayTitle: String? = null,
        overlayTextSize: Float = 12f,
        overlayHeight: Int = 70,
        overlayBottomMargin: Int = 56
    ): FrameLayout {
        return FrameLayout(this).apply {
            isClickable = true
            isFocusable = true
            contentDescription = titleText

            addView(
                ImageView(this@VideoListActivity).apply {
                    setImageResource(drawableId)
                    adjustViewBounds = true
                    scaleType = ImageView.ScaleType.FIT_CENTER
                },
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )

            overlayTitle?.let { label ->
                addView(
                    TextView(this@VideoListActivity).apply {
                        text = label
                        textSize = overlayTextSize
                        typeface = Typeface.create(AppStyle.titleFont, Typeface.BOLD)
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER
                        includeFontPadding = true
                        maxLines = 2
                        setLineSpacing(0f, 0.95f)
                        setPadding(22, 0, 22, 0)
                    },
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        overlayHeight,
                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    ).apply {
                        bottomMargin = overlayBottomMargin
                    }
                )
            }

            setOnClickListener { action() }
        }
    }

    private fun walkingButtonTitle(titleText: String): String {
        val upperTitle = titleText.uppercase()
        if (upperTitle.contains("-")) {
            return upperTitle.replace("-", "\n")
        }

        val words = upperTitle.split(" ")
        return when {
            words.size == 2 -> words.joinToString("\n")
            words.size > 2 -> "${words.dropLast(1).joinToString(" ")}\n${words.last()}"
            else -> upperTitle
        }
    }

    private fun walkingButtonTextSize(titleText: String): Float {
        val longestLine = walkingButtonTitle(titleText)
            .lines()
            .maxOfOrNull { it.length }
            ?: titleText.length

        return when {
            longestLine > 16 -> 10.5f
            longestLine > 12 -> 11.5f
            else -> 12.5f
        }
    }

    private fun addWalkingVideoButtons(videos: List<CloudMediaItem>) {
        videos.chunked(2).forEach { rowVideos ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            rowVideos.forEach { video ->
                val rowAction = {
                    val intent = Intent(this, VideoActivity::class.java)
                    intent.putExtra(VideoActivity.EXTRA_MEDIA_URL, video.url)
                    startActivity(intent)
                }

                row.addView(
                    walkingVideoButton(video.title, rowAction),
                    LinearLayout.LayoutParams(
                        0,
                        430,
                        1f
                    ).apply {
                        setMargins(6, 6, 6, 14)
                    }
                )
            }

            if (rowVideos.size == 1) {
                row.addView(
                    View(this),
                    LinearLayout.LayoutParams(
                        0,
                        430,
                        1f
                    ).apply {
                        setMargins(6, 6, 6, 14)
                    }
                )
            }

            listContainer.addView(
                row,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    private fun categoryButton(category: String): FrameLayout {
        return when {
            category.equals("Walking Videos", ignoreCase = true) -> {
                imageButton(
                    drawableId = R.drawable.walking_video_button_template,
                    titleText = category,
                    action = { showVideos(category) },
                    overlayTitle = "WALKING\nVIDEOS",
                    overlayTextSize = 13.5f,
                    overlayHeight = 130,
                    overlayBottomMargin = 76
                )
            }

            category.equals("Conv Videos", ignoreCase = true) -> {
                imageButton(
                    drawableId = R.drawable.conversation_videos_category_button,
                    titleText = category,
                    action = { showVideos(category) }
                )
            }

            category.equals("PTP Videos", ignoreCase = true) -> {
                imageButton(
                    drawableId = R.drawable.ptp_videos_category_button,
                    titleText = category,
                    action = { showVideos(category) }
                )
            }

            else -> {
                imageButton(
                    drawableId = R.drawable.walking_video_button_template,
                    titleText = category,
                    action = { showVideos(category) },
                    overlayTitle = category.uppercase(),
                    overlayTextSize = 14f
                )
            }
        }
    }

    private fun addCategoryButtons() {
        categories.forEach { category ->
            listContainer.addView(
                categoryButton(category),
                LinearLayout.LayoutParams(
                    520,
                    520
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    setMargins(0, 8, 0, 18)
                }
            )
        }
    }

    private fun addPtpVideoButtons(videos: List<CloudMediaItem>) {
        videos.chunked(2).forEach { rowVideos ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            rowVideos.forEach { video ->
                val rowAction = {
                    val intent = Intent(this, VideoActivity::class.java)
                    intent.putExtra(VideoActivity.EXTRA_MEDIA_URL, video.url)
                    startActivity(intent)
                }

                row.addView(
                    imageButton(
                        drawableId = R.drawable.ptp_video_button_template,
                        titleText = video.title,
                        action = rowAction
                    ),
                    LinearLayout.LayoutParams(
                        0,
                        430,
                        1f
                    ).apply {
                        setMargins(6, 6, 6, 14)
                    }
                )
            }

            if (rowVideos.size == 1) {
                row.addView(
                    View(this),
                    LinearLayout.LayoutParams(
                        0,
                        430,
                        1f
                    ).apply {
                        setMargins(6, 6, 6, 14)
                    }
                )
            }

            listContainer.addView(
                row,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    private fun backButton(): TextView {
        return TextView(this).apply {
            text = "< Back"
            textSize = 22f
            typeface = AppStyle.titleFont
            setTextColor(AppStyle.white)
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
            setBackgroundColor(AppStyle.card)
            setOnClickListener { finish() }
        }
    }

    private fun showCategories() {
        selectedCategory = null
        header.text = "Videos"
        footerButton.text = "< Back"
        footerButton.setOnClickListener { finish() }
        listContainer.removeAllViews()

        val videos = catalog?.videos ?: emptyList()
        if (videos.isEmpty()) {
            showMessage("No Cloudflare videos are configured yet.")
            return
        }

        addCategoryButtons()
    }

    private fun showVideos(category: String) {
        selectedCategory = category
        header.text = category
        footerButton.text = "< Videos"
        footerButton.setOnClickListener { showCategories() }
        listContainer.removeAllViews()

        val videos = catalog?.videos
            ?.filter { it.category.equals(category, ignoreCase = true) }
            ?: emptyList()

        if (videos.isEmpty()) {
            showMessage("No videos are in this folder yet.")
            return
        }

        if (category.equals("Walking Videos", ignoreCase = true)) {
            addWalkingVideoButtons(videos)
        } else if (category.equals("PTP Videos", ignoreCase = true)) {
            addPtpVideoButtons(videos)
        } else {
            videos.forEach { video ->
                val rowAction = {
                    val intent = Intent(this, VideoActivity::class.java)
                    intent.putExtra(VideoActivity.EXTRA_MEDIA_URL, video.url)
                    startActivity(intent)
                }

                listContainer.addView(mediaRow("", video.title, rowAction))
            }
        }
    }

    private fun showMessage(message: String) {
        listContainer.removeAllViews()
        listContainer.addView(
            TextView(this).apply {
                text = message
                textSize = 18f
                typeface = AppStyle.monoFont
                setTextColor(AppStyle.subtext)
                setPadding(8, 8, 8, 24)
            }
        )
    }
}
