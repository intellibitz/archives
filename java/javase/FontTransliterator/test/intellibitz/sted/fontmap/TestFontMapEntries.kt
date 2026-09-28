package intellibitz.sted.fontmap

class TestFontMapEntries {
    constructor() : super()

    companion object {
        @JvmStatic
                fun testAdd() {
                var i: Int = 1
                val entries: FontMapEntries = FontMapEntries()
                val entry1: FontMapEntry = FontMapEntry("a", "b")
                entry1.setBeginsWith(true)
                entries.add(entry1)
                assert (entries.size() == i++)
                val entry2: FontMapEntry = FontMapEntry("a", "b")
                entry2.setEndsWith(true)
                entries.add(entry2)
                assert (entries.size() == i++)
                val entry3: FontMapEntry = FontMapEntry("a", "b")
                entry3.setBeginsWith(true)
                entry3.setEndsWith(true)
                entries.add(entry3)
                assert (entries.size() == i++)
                val entry4: FontMapEntry = FontMapEntry("a", "b")
                entry4.setFollowedBy("a")
                entries.add(entry4)
                assert (entries.size() == i++)
                val entry5: FontMapEntry = FontMapEntry("a", "b")
                entry5.setPrecededBy("a")
                entries.add(entry5)
                assert (entries.size() == i++)
                val entry6: FontMapEntry = FontMapEntry("a", "b")
                entry6.setFollowedBy("a")
                entry6.setPrecededBy("a")
                entries.add(entry6)
                assert (entries.size() == i++)
                val entry7: FontMapEntry = FontMapEntry("a", "b")
                entry7.setBeginsWith(true)
                entry7.setFollowedBy("a")
                entry7.setPrecededBy("a")
                entries.add(entry7)
                assert (entries.size() == i++)
                val entry8: FontMapEntry = FontMapEntry("a", "b")
                entry8.setBeginsWith(true)
                entry8.setEndsWith(true)
                entry8.setFollowedBy("a")
                entry8.setPrecededBy("a")
                entries.add(entry8)
                assert (entries.size() == i++)
                val entry9: FontMapEntry = FontMapEntry("a", "b")
                entries.add(entry9)
                assert (entries.size() == i)
            }
    }
}
