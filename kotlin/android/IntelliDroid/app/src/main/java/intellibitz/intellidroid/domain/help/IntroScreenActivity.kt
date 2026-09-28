package intellibitz.intellidroid.domain.help

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import intellibitz.intellidroid.R

@SuppressLint("InlinedApi")
class IntroScreenActivity : AppCompatActivity() {

    private var btnSkip: Button? = null
    private var btnDone: Button? = null
    private var btnNext: ImageButton? = null
    private var viewpager: ViewPager? = null
    private var pagerAdapter: PagerAdapter? = null
    private var circles: LinearLayout? = null
    private var isOpaque = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val window = window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        )
        setContentView(R.layout.welcome_layout)

        btnSkip = findViewById<Button>(R.id.btn_skip).apply {
            setOnClickListener { endIntroduction() }
        }

        viewpager = findViewById(R.id.pager)

        btnNext = findViewById<ImageButton>(R.id.btn_login).apply {
            setOnClickListener {
                viewpager?.let { vp ->
                    vp.setCurrentItem(vp.currentItem + 1, true)
                }
            }
        }

        btnDone = findViewById<Button>(R.id.done).apply {
            setOnClickListener { endIntroduction() }
        }

        val adapter = ScreenSlideAdapter(supportFragmentManager)
        pagerAdapter = adapter
        viewpager?.adapter = adapter

        viewpager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (position == TOTAL_PAGES - 2 && positionOffset > 0) {
                    if (isOpaque) {
                        viewpager?.setBackgroundColor(Color.TRANSPARENT)
                        isOpaque = false
                    }
                } else {
                    if (!isOpaque) {
                        viewpager?.setBackgroundColor(
                            ContextCompat.getColor(this@IntroScreenActivity, R.color.primary_light)
                        )
                        isOpaque = true
                    }
                }
            }

            override fun onPageSelected(position: Int) {
                setIndicator(position)
                if (position == TOTAL_PAGES - 2) {
                    btnSkip?.visibility = View.GONE
                    btnNext?.visibility = View.GONE
                    btnDone?.visibility = View.VISIBLE
                } else if (position < TOTAL_PAGES - 2) {
                    btnSkip?.visibility = View.VISIBLE
                    btnNext?.visibility = View.VISIBLE
                    btnDone?.visibility = View.GONE
                } else if (position == TOTAL_PAGES - 1) {
                    endIntroduction()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        buildCircles()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewpager?.clearOnPageChangeListeners()
    }

    private fun buildCircles() {
        circles = findViewById(R.id.circles)

        val scale = resources.displayMetrics.density
        val padding = (5 * scale + 0.5f).toInt()

        for (i in 0 until TOTAL_PAGES - 1) {
            val circle = ImageView(this).apply {
                setImageResource(R.drawable.ic_checkbox_blank_circle_white_18dp)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                adjustViewBounds = true
                setPadding(padding, 0, padding, 0)
            }
            circles?.addView(circle)
        }

        setIndicator(0)
    }

    private fun setIndicator(index: Int) {
        val circlesLayout = circles ?: return
        if (index < TOTAL_PAGES) {
            for (i in 0 until TOTAL_PAGES - 1) {
                val circle = circlesLayout.getChildAt(i) as? ImageView ?: continue
                if (i == index) {
                    circle.setColorFilter(ContextCompat.getColor(this, R.color.text_selected))
                } else {
                    circle.setColorFilter(ContextCompat.getColor(this, R.color.transparent_bg))
                }
            }
        }
    }

    private fun endIntroduction() {
        finish()
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val vp = viewpager
        if (vp == null || vp.currentItem == 0) {
            super.onBackPressed()
        } else {
            vp.currentItem = vp.currentItem - 1
        }
    }

    @Suppress("DEPRECATION")
    private inner class ScreenSlideAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> IntroScreenFragment.newInstance(R.layout.fragment_screen1)
                1 -> IntroScreenFragment.newInstance(R.layout.fragment_screen2)
                2 -> IntroScreenFragment.newInstance(R.layout.fragment_screen3)
                3 -> IntroScreenFragment.newInstance(R.layout.fragment_screen4)
                else -> IntroScreenFragment.newInstance(R.layout.fragment_screen1)
            }
        }

        override fun getCount(): Int = TOTAL_PAGES
    }

    companion object {
        const val TOTAL_PAGES = 4
    }
}
