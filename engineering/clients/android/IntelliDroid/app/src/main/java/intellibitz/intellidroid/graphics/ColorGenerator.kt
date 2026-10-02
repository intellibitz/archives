package intellibitz.intellidroid.graphics

import android.widget.ImageView
import java.util.Random

class ColorGenerator private constructor(private val mColors: List<Int>) {
    private val mRandom: Random = Random(System.currentTimeMillis())

    val randomColor: Int
        get() = mColors[mRandom.nextInt(mColors.size)]

    fun getColor(key: Any): Int {
        return mColors[Math.abs(key.hashCode()) % mColors.size]
    }

    companion object {
        @JvmField
        val DEFAULT: ColorGenerator = create(
            listOf(
                -0xee9c,
                -0xa7aa7,
                -0x65bc2,
                -0x1b39d2,
                -0x98408c,
                -0xa65d42,
                -0xdf6c33,
                -0x529d59,
                -0x7fa87f
            )
        )

        @JvmField
        val MATERIAL: ColorGenerator = create(
            listOf(
                -0x1a8c8d,
                -0xf9d6e,
                -0x459738,
                -0x6a8a33,
                -0x867935,
                -0x9b4a0a,
                -0xb03c09,
                -0xb22f1f,
                -0xb24954,
                -0x7e387c,
                -0x512a7f,
                -0x759b,
                -0x2b1ea9,
                -0x2ab1,
                -0x48b3,
                -0x5e7781,
                -0x6f5b52
            )
        )

        @JvmStatic
        fun create(colorList: List<Int>): ColorGenerator {
            return ColorGenerator(colorList)
        }

        @JvmStatic
        fun getTextDrawable(value: String?): TextDrawable {
            val generator = MATERIAL
            var textVal = value
            val color = if (textVal == null) {
                textVal = "AK"
                generator.randomColor
            } else {
                generator.getColor(textVal)
            }

            val builder = TextDrawable.builder()
                .beginConfig()
                .withBorder(4)
                .endConfig()
                .round()

            return builder.build(textVal.substring(0, 1), color)
        }

        @JvmStatic
        fun setTextDrawable(view: ImageView, `val`: String?) {
            view.setImageDrawable(getTextDrawable(`val`))
        }
    }
}
