package com.retailwave.fce.server.service;
/**
 * $Id: UserServiceTest.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/test/java/integration/com/retailwave/fce/server/service/UserServiceTest.java $
 */

import com.retailwave.fce.shared.domain.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.List;

@ContextConfiguration({
        "classpath*:/WEB-INF/applicationContext.xml",
        "classpath*:/WEB-INF/applicationContext*.xml"
})
public class UserServiceTest extends AbstractJUnit4SpringContextTests {

    private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void testSearchUser() {
        User user = new User();
        user.setName("pcarey");
//        assertNotNull(userService);
        List<User> users = userService.search(user);
//        logger.info("UserDTO results for : " + user);
        for (User user1 : users) {
//            logger.info(user1);
        }
    }

}
