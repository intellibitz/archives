package intellibitz.sted.fontmap

class TestFontMapEntry {
    constructor() : super()

    companion object {
        @JvmStatic
                fun testCompareTo1() {
                val entry1: FontMapEntry = FontMapEntry("a", "b")
                val entry2: FontMapEntry = FontMapEntry("a", "b")
                assert (entry1 == entry2)
                assert (entry1.compareTo(entry2) == 0)
                entry1.setBeginsWith(true)
                assert (entry1.compareTo(entry2) != 0)
                entry2.setBeginsWith(true)
                assert (entry1.compareTo(entry2) == 0)
                entry2.setEndsWith(true)
                assert (entry1.compareTo(entry2) != 0)
                entry1.setEndsWith(true)
                assert (entry1.compareTo(entry2) == 0)
                entry1.setPrecededBy("a")
                assert (entry1.compareTo(entry2) != 0)
                entry2.setPrecededBy("a")
                assert (entry1.compareTo(entry2) == 0)
                entry1.setFollowedBy("a")
                assert (entry1.compareTo(entry2) != 0)
                entry2.setFollowedBy("a")
                assert (entry1.compareTo(entry2) == 0)
            }
        @JvmStatic
                fun testCompareTo2() {
                val entry1: FontMapEntry = FontMapEntry("a", "b")
                val entry2: FontMapEntry = FontMapEntry("a", "b")
                entry1.setPrecededBy("a")
                assert (entry1.compareTo(entry2) != 0)
                entry2.setPrecededBy("a")
                assert (entry1.compareTo(entry2) == 0)
                entry1.setFollowedBy("a")
                assert (entry1.compareTo(entry2) != 0)
                entry1.setBeginsWith(true)
                assert (entry1.compareTo(entry2) != 0)
                entry1.setEndsWith(true)
                assert (entry1.compareTo(entry2) != 0)
            }
        @JvmStatic
                fun testEquals() {
                val entry1: FontMapEntry = FontMapEntry("a", "b")
                val entry2: FontMapEntry = FontMapEntry("a", "b")
                assert (entry1.equals(entry2))
                entry1.setPrecededBy("a")
                assert (!entry1.equals(entry2))
                entry2.setPrecededBy("a")
                assert (entry1.equals(entry2))
            }
    }
}
