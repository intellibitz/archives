package intellibitz.sted.fontmap;

/**
 * Created by IntelliJ IDEA. User: Muthu Ramadoss Date: Nov 4, 2003 Time:
 * 11:49:35 PM To change this template use Options | File Templates.
 */
public class TestFontMapEntries
{

    public TestFontMapEntries()
    {
    }


    public static void testAdd()
    {
        int i = 1;
        final FontMapEntries entries = new FontMapEntries();
        final FontMapEntry entry1 = new FontMapEntry("a", "b");
        entry1.setBeginsWith(true);
        entries.add(entry1);
        assert (entries.size() == i++);
        final FontMapEntry entry2 = new FontMapEntry("a", "b");
        entry2.setEndsWith(true);
        entries.add(entry2);
        assert (entries.size() == i++);
        final FontMapEntry entry3 = new FontMapEntry("a", "b");
        entry3.setBeginsWith(true);
        entry3.setEndsWith(true);
        entries.add(entry3);
        assert (entries.size() == i++);
        final FontMapEntry entry4 = new FontMapEntry("a", "b");
        entry4.setFollowedBy("a");
        entries.add(entry4);
        assert (entries.size() == i++);
        final FontMapEntry entry5 = new FontMapEntry("a", "b");
        entry5.setPrecededBy("a");
        entries.add(entry5);
        assert (entries.size() == i++);
        final FontMapEntry entry6 = new FontMapEntry("a", "b");
        entry6.setFollowedBy("a");
        entry6.setPrecededBy("a");
        entries.add(entry6);
        assert (entries.size() == i++);
        final FontMapEntry entry7 = new FontMapEntry("a", "b");
        entry7.setBeginsWith(true);
        entry7.setFollowedBy("a");
        entry7.setPrecededBy("a");
        entries.add(entry7);
        assert (entries.size() == i++);
        final FontMapEntry entry8 = new FontMapEntry("a", "b");
        entry8.setBeginsWith(true);
        entry8.setEndsWith(true);
        entry8.setFollowedBy("a");
        entry8.setPrecededBy("a");
        entries.add(entry8);
        assert (entries.size() == i++);
        final FontMapEntry entry9 = new FontMapEntry("a", "b");
        entries.add(entry9);
        assert (entries.size() == i);
    }

}
