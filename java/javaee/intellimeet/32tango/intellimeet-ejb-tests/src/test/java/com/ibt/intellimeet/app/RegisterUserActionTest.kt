package com.ibt.intellimeet.app

import org.jboss.seam.mock.BaseSeamTest
import org.jboss.seam.mock.SeamTest
import org.testng.annotations.Test
import java.util.logging.Logger

class RegisterUserActionTest : SeamTest() {

    var log: Logger = Logger.getLogger(RegisterUserActionTest::class.java.name)

    @Test
    @Throws(Exception::class)
    fun testSeamComponents() {
        object : BaseSeamTest.FacesRequest(
            "/app-ejb-tests/registerUserActionTest.xhtml"
        ) {
            @Throws(Exception::class)
            override fun invokeApplication() {
                setValue("#{user.email}", "test1@test.com")
                setValue("#{user.password}", "test1")
                setValue("#{registerUserAction.verify}", "test1")
                val id = invokeAction("#{registerUserAction.register}")
                log.info("TEST: Registred User with id: $id")
            }
        }.run()
    }
}
