package intellibitz.sted.io;

public class TestFileReaderThread {

    public TestFileReaderThread() {
    }


    public static void testViewToolBar() {
        final FileReaderThread fileReaderThread = new FileReaderThread(null);
        try {
            fileReaderThread.start();
        } catch (IllegalStateException e) {
            System.out.println("Caught Exception");
            e.printStackTrace();
        }
        System.out.println("After Start");
    }

}
