package intellibitz.intellidroid

import android.database.sqlite.SQLiteDatabase
import android.test.ProviderTestCase2
import android.test.mock.MockContentResolver
import androidx.test.ext.junit.runners.AndroidJUnit4
import intellibitz.intellidroid.content.UserContentProvider
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Constructor.
 *
 * @ The class name of the provider under test
 * @ The provider's authority string
 */
@RunWith(AndroidJUnit4::class)
class SampleProviderTestCase2 : ProviderTestCase2<content.UserContentProvider>(
    content.UserContentProvider::class.java, content.UserContentProvider.AUTHORITY
) {
    // Contains a reference to the mocked content resolver for the provider under test.
    private var mMockResolver: MockContentResolver? = null

    // Contains an SQLite database, used as test data
    private var mDb: SQLiteDatabase? = null

    /*
     * Sets up the test environment before each test method. Creates a mock content resolver,
     * gets the provider under test, and creates a new database for the provider.
     */
    @Throws(Exception::class)
    override fun setUp() {
//        setContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
        // Calls the base class implementation of this method.
//        super.setUp()

        // Gets the resolver for this test.
        mMockResolver = mockContentResolver

        /*
         * Gets a handle to the database underlying the provider. Gets the provider instance
         * created in super.setUp(), gets the DatabaseOpenHelper for the provider, and gets
         * a database object from the helper.
         */mDb = provider!!.openHelperForTest.writableDatabase
    }

    /*
     *  This method is called after each test method, to clean up the current fixture. Since
     *  this sample test case runs in an isolated context, no cleanup is necessary.
     */
    @Throws(Exception::class)
    override fun tearDown() {
//        super.tearDown()
    }

    @Test
    fun testProviders() {
        assert(mMockResolver != null)
        assert(mDb != null)
    }
}