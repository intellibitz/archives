package intellibitz.sted.fontmap;

/**
 * Created by IntelliJ IDEA. User: Muthu Ramadoss Date: Nov 4, 2003 Time:
 * 11:49:35 PM To change this template use Options | File Templates.
 */
public class TestFontMapEntry {

    public TestFontMapEntry() {
    }


    public static void testCompareTo1() {
        final FontMapEntry entry1 = new FontMapEntry("a", "b");
        final FontMapEntry entry2 = new FontMapEntry("a", "b");
        assert (entry1 == entry2);
        assert (entry1.compareTo(entry2) == 0);
        entry1.setBeginsWith(true);
        assert (entry1.compareTo(entry2) != 0);
        entry2.setBeginsWith(true);
        assert (entry1.compareTo(entry2) == 0);
        entry2.setEndsWith(true);
        assert (entry1.compareTo(entry2) != 0);
        entry1.setEndsWith(true);
        assert (entry1.compareTo(entry2) == 0);
        entry1.setPrecededBy("a");
        assert (entry1.compareTo(entry2) != 0);
        entry2.setPrecededBy("a");
        assert (entry1.compareTo(entry2) == 0);
        entry1.setFollowedBy("a");
        assert (entry1.compareTo(entry2) != 0);
        entry2.setFollowedBy("a");
        assert (entry1.compareTo(entry2) == 0);
    }

    public static void testCompareTo2() {
        final FontMapEntry entry1 = new FontMapEntry("a", "b");
        final FontMapEntry entry2 = new FontMapEntry("a", "b");
        entry1.setPrecededBy("a");
        assert (entry1.compareTo(entry2) != 0);
        entry2.setPrecededBy("a");
        assert (entry1.compareTo(entry2) == 0);
        entry1.setFollowedBy("a");
        assert (entry1.compareTo(entry2) != 0);
        entry1.setBeginsWith(true);
        assert (entry1.compareTo(entry2) != 0);
        entry1.setEndsWith(true);
        assert (entry1.compareTo(entry2) != 0);
    }

    public static void testEquals() {
        final FontMapEntry entry1 = new FontMapEntry("a", "b");
        final FontMapEntry entry2 = new FontMapEntry("a", "b");
        assert (entry1.equals(entry2));
        entry1.setPrecededBy("a");
        assert (!entry1.equals(entry2));
        entry2.setPrecededBy("a");
        assert (entry1.equals(entry2));
    }
}
