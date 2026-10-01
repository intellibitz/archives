package intellibitz.intellidroid

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import intellibitz.intellidroid.db.DatabaseHelper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [Testing Fundamentals](http://d.android.com/tools/testing/testing_android.html)
 */
@RunWith(AndroidJUnit4::class)
class SampleApplicationTestCase {
    @Before
    fun setUp() {
//        setContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
//        super.setUp();
    }

    @Test
    fun intellibitzSQLiteOpenHelper() {
        val db = db.DatabaseHelper.newInstance(
            InstrumentationRegistry.getInstrumentation().targetContext,
            "UID_123456"
        )

/*
        long topic1_id = db.createTopic("Topic 1", "Wa wa wa topic 1");
        long topic2_id = db.createTopic("Topic 2", "Wa wa wa topic 2");
        long topic3_id = db.createTopic("Topic 3", "Wa wa wa topic 3");
        long topic4_id = db.createTopic("Topic 4", "Wa wa wa topic 4");

        Log.d("Tag Count", "Tag Count: " + db.getAllTopics().size());

        long msg1_id = db.createMessage("Message 1", "Har har message 1", new long[]{topic1_id});
        long msg2_id = db.createMessage("Message 2", "Har har message 2", new long[]{topic2_id});
        long msg3_id = db.createMessage("Message 3", "Har har message 3", new long[]{topic3_id});
        long msg4_id = db.createMessage("Message 4", "Har har message 4", new long[]{topic4_id});

        Log.e("Todo Count", "Todo count: " + db.getMessagesCount());

        db.createTopicMessage(topic1_id, msg1_id);

        // Getting all tag names
        Log.d("Get Topics", "Getting All Topics");

        List<String> allTags = db.getAllTopics();
        for (String tag : allTags) {
            Log.d("Topic Name", tag);
        }

        // Getting all Todos
        Log.d("Get Messages", "Getting All Messages");

        List<String> allToDos = db.getAllMessages();
        for (String todo : allToDos) {
            Log.d("Message", todo);
        }

        // Getting todos under "Watchlist" tag name
        Log.d("ToDo", "Get todos under single Tag name");

        List<String> tagsWatchList = db.getAllMessagesByTopic("Topic 1");
        for (String todo : tagsWatchList) {
            Log.d("Topic 1 message:", todo);
        }

        // Updating tag name
        db.updateTopic(topic2_id, "Topic 2", "Topic 2 Updated");

        // Deleting a Message
        Log.d("Delete ToDo", "Deleting a Todo");
        Log.d("Tag Count", "Tag Count Before Deleting: " + db.getMessagesCount());

        db.deleteMessage("Message 1");

        Log.d("Topics Count", "Topics Count After Deleting: " + db.getMessagesCount());

        // Deleting all Todos under "Shopping" tag
        Log.d("Tag Count",
                "Tag Count Before Deleting 'Shopping' Todos: "
                        + db.getMessagesCount());

        db.deleteTopic("Topic 1", true);
        db.deleteTopic(topic2_id, "Topic 2", true);
        db.deleteTopic(topic3_id, "Topic 3", true);
        db.deleteTopic("Topic 4", true);

        Log.d("Count", "Topics and Messages: " + db.getTopicsCount() + " && "
                + db.getMessagesCount());

*/

        // Don't forget to close database connection
        db.closeDB()
    }
}