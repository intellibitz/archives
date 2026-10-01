package com.retailwave.fce.server.service
/**
 * $Id: UserServiceTest.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/test/java/integration/com/retailwave/fce/server/service/UserServiceTest.java $
 */

import com.retailwave.fce.shared.domain.User
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests

@ContextConfiguration(
    "classpath*:/WEB-INF/applicationContext.xml",
    "classpath*:/WEB-INF/applicationContext*.xml"
)
class UserServiceTest : AbstractJUnit4SpringContextTests() {

    private var userService: UserService? = null

    fun setUserService(userService: UserService?) {
        this.userService = userService
    }

    fun testSearchUser() {
        val user = User()
        user.name = "pcarey"
        val users = userService!!.search(user)
        for (user1 in users) {
//            logger.info(user1);
        }
    }
}
