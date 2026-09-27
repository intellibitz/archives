package intellibitz.intellidroid.graphics;

import android.widget.ImageView;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ColorGenerator {

    public static ColorGenerator DEFAULT;

    public static ColorGenerator MATERIAL;

    static {
        DEFAULT = create(Arrays.asList(
                0xfff16364,
                0xfff58559,
                0xfff9a43e,
                0xffe4c62e,
                0xff67bf74,
                0xff59a2be,
                0xff2093cd,
                0xffad62a7,
                0xff805781
        ));
        MATERIAL = create(Arrays.asList(
                0xffe57373,
                0xfff06292,
                0xffba68c8,
                0xff9575cd,
                0xff7986cb,
                0xff64b5f6,
                0xff4fc3f7,
                0xff4dd0e1,
                0xff4db6ac,
                0xff81c784,
                0xffaed581,
                0xffff8a65,
                0xffd4e157,
                0xffffd54f,
                0xffffb74d,
                0xffa1887f,
                0xff90a4ae
        ));
    }

    private final List<Integer> mColors;
    private final Random mRandom;

    private ColorGenerator(List<Integer> colorList) {
        mColors = colorList;
        mRandom = new Random(System.currentTimeMillis());
    }

    public static ColorGenerator create(List<Integer> colorList) {
        return new ColorGenerator(colorList);
    }

    public static TextDrawable getTextDrawable(String val) {
        ColorGenerator generator = MATERIAL; // or use DEFAULT
// generate random color
        int color = generator.getRandomColor();
// generate color based on a key (same key returns the same color), useful for list/grid views
        if (null == val) {
            val = "AK";
        } else {
            color = generator.getColor(val);
        }

// declare the builder object once.
        TextDrawable.IBuilder builder = TextDrawable.builder()
                .beginConfig()
                .withBorder(4)
                .endConfig()
                .round();

// reuse the builder specs to createFileInES multiple drawables
//                    TextDrawable ic1 = builder.build("A", color1);
        return builder.build(val.substring(0, 1), color);
    }

    public static void setTextDrawable(ImageView view, String val) {
        view.setImageDrawable(getTextDrawable(val));
    }

    public int getRandomColor() {
        return mColors.get(mRandom.nextInt(mColors.size()));
    }

    public int getColor(Object key) {
        return mColors.get(Math.abs(key.hashCode()) % mColors.size());
    }
}
