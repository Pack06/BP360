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

class PictureListActivity : ComponentActivity() {

    private val categories = listOf(
        "Biblical Sites",
        "Extrabiblical Sites",
        "Reconstructed Models"
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
                setImageResource(R.drawable.photos_background)
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
            text = "Photos"
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

        showMessage("Loading pictures...")

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

            val text = TextView(this@PictureListActivity).apply {
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

    private fun photoButton(drawableId: Int, titleText: String, action: () -> Unit): FrameLayout {
        return FrameLayout(this).apply {
            isClickable = true
            isFocusable = true
            contentDescription = titleText

            addView(
                ImageView(this@PictureListActivity).apply {
                    setImageResource(drawableId)
                    adjustViewBounds = true
                    scaleType = ImageView.ScaleType.FIT_CENTER
                },
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )

            addView(
                TextView(this@PictureListActivity).apply {
                    text = photoButtonTitle(titleText)
                    textSize = photoButtonTextSize(titleText)
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
                    118,
                    Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                ).apply {
                    bottomMargin = 50
                }
            )

            setOnClickListener { action() }
        }
    }

    private fun photoButtonTitle(titleText: String): String {
        return when (titleText) {
            "Pyramids of Egypt" -> "PYRAMIDS\nOF EGYPT"
            "Tomb of Ramses I" -> "TOMB OF\nRAMSES I"
            else -> {
                val words = titleText.uppercase().split(" ")
                when {
                    words.size == 2 -> words.joinToString("\n")
                    words.size > 2 -> "${words.dropLast(1).joinToString(" ")}\n${words.last()}"
                    else -> titleText.uppercase()
                }
            }
        }
    }

    private fun photoButtonTextSize(titleText: String): Float {
        val longestLine = photoButtonTitle(titleText)
            .lines()
            .maxOfOrNull { it.length }
            ?: titleText.length

        return when {
            longestLine > 16 -> 10.5f
            longestLine > 12 -> 11.5f
            else -> 12.5f
        }
    }

    private fun addPhotoButtons(pictures: List<CloudMediaItem>, drawableId: Int) {
        pictures.chunked(2).forEach { rowPictures ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            rowPictures.forEach { picture ->
                val rowAction = {
                    val intent = Intent(this, PictureActivity::class.java)
                    intent.putExtra(PictureActivity.EXTRA_MEDIA_URL, picture.url)
                    startActivity(intent)
                }

                row.addView(
                    photoButton(drawableId, picture.title, rowAction),
                    LinearLayout.LayoutParams(
                        0,
                        430,
                        1f
                    ).apply {
                        setMargins(6, 6, 6, 14)
                    }
                )
            }

            if (rowPictures.size == 1) {
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

    private fun photoCategoryDrawable(category: String): Int {
        return when {
            category.equals("Biblical Sites", ignoreCase = true) ->
                R.drawable.biblical_photo_button_template

            category.equals("Extrabiblical Sites", ignoreCase = true) ->
                R.drawable.extrabiblical_photo_button_template

            category.equals("Reconstructed Models", ignoreCase = true) ->
                R.drawable.models_photo_button_template

            else -> R.drawable.biblical_photo_button_template
        }
    }

    private fun addPhotoCategoryButtons() {
        categories.forEach { category ->
            listContainer.addView(
                photoButton(photoCategoryDrawable(category), category) {
                    showPictures(category)
                },
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
        header.text = "Photos"
        footerButton.text = "< Back"
        footerButton.setOnClickListener { finish() }
        listContainer.removeAllViews()

        val pictures = catalog?.pictures ?: emptyList()
        if (pictures.isEmpty()) {
            showMessage("No Cloudflare pictures are configured yet.")
            return
        }

        addPhotoCategoryButtons()
    }

    private fun showPictures(category: String) {
        selectedCategory = category
        header.text = category
        footerButton.text = "< Photos"
        footerButton.setOnClickListener { showCategories() }
        listContainer.removeAllViews()

        val pictures = catalog?.pictures
            ?.filter { it.category.equals(category, ignoreCase = true) }
            ?: emptyList()

        if (pictures.isEmpty()) {
            showMessage("No photos are in this folder yet.")
            return
        }

        if (category.equals("Biblical Sites", ignoreCase = true)) {
            addPhotoButtons(pictures, R.drawable.biblical_photo_button_template)
        } else if (category.equals("Extrabiblical Sites", ignoreCase = true)) {
            addPhotoButtons(pictures, R.drawable.extrabiblical_photo_button_template)
        } else if (category.equals("Reconstructed Models", ignoreCase = true)) {
            addPhotoButtons(pictures, R.drawable.models_photo_button_template)
        } else {
            pictures.forEach { picture ->
                listContainer.addView(mediaRow("", picture.title) {
                    val intent = Intent(this, PictureActivity::class.java)
                    intent.putExtra(PictureActivity.EXTRA_MEDIA_URL, picture.url)
                    startActivity(intent)
                })
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
