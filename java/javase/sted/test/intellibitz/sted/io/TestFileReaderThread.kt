package intellibitz.sted.io

class TestFileReaderThread {
    constructor() : super()

    companion object {
        @JvmStatic
                fun testViewToolBar() {
                val fileReaderThread: FileReaderThread = FileReaderThread(null)
                try
                {
                    fileReaderThread.start()
                }
                catch (e: IllegalStateException)
                {
                    System.out.println("Caught Exception")
                    e.printStackTrace()
                }
                System.out.println("After Start")
            }
    }
}
