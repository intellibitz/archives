package com.ibt.intellidocs.dummy

import org.jboss.seam.mock.BaseSeamTest
import org.jboss.seam.mock.SeamTest
import org.testng.annotations.Test
import java.util.logging.Logger

class DummyUserActionTest : SeamTest() {

    var log: Logger = Logger.getLogger(DummyUserActionTest::class.java.name)

    @Test
    @Throws(Exception::class)
    fun testSeamComponents() {
        object : BaseSeamTest.FacesRequest("/dummy/registerDummyUser.xhtml") {
            @Throws(Exception::class)
            override fun invokeApplication() {
                setValue("#{dummyUser.name}", "test1")
                setValue("#{dummyUser.username}", "test1")
                setValue("#{dummyUser.password}", "test1")
                setValue("#{dummyUserAction.verify}", "test1")
                val id = invokeAction("#{dummyUserAction.register}")
                log.info("TEST: Registred User with id: $id")
            }
        }.run()
    }
}
