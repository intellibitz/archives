package com.ibt.intellimeet.data

/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
$HeadURL::                                                                      $: Head URL of last commit
-->
*/

import com.ibt.intellimeet.ejb.EJBTest
import org.testng.annotations.Test
import java.util.logging.Logger
import javax.naming.InitialContext
import javax.persistence.EntityManager
import javax.transaction.TransactionManager

/**
 * UserTest Unit test for the User Entity
 */
class UserTest : EJBTest() {

    var log: Logger = Logger.getLogger("UserTest")

    @Test
    @Throws(Exception::class)
    fun testUser() {
        object : ComponentTest() {
            @Throws(Exception::class)
            override fun testComponents() {
                val em = InitialContext().lookup(
                    "java:/EntityManagers/DefaultDS"
                ) as EntityManager

                // Obtain JBoss transaction
                val tm = InitialContext().lookup(
                    "java:/TransactionManager"
                ) as TransactionManager

                // testing CREATION
                tm.begin()

                var user = User()
                user.email = "test1@test.com"
                user.password = "test1"
                assert(user.id == 0L)
                em.persist(user)
                tm.commit()

                assert(user.id > 0)
                val id = user.id
                log.info("created user 'test1@test.com' in DB with id: $id")

                // testing RETREIVAL
                tm.begin()
                user = em.find(User::class.java, id)
                assert(null != user)
                assert(user.id > 0)
                assert("test1" == user.password)
                assert("test1@test.com" == user.email)
                tm.commit()
                log.info("found user 'test1@test.com' in DB with id: $id")

                // testing REMOVE
/*
                tm.begin();
                em.refresh(user);
                em.remove(user);
                log.info (user.toString());
                tm.commit();
                log.info("removed user 'test1@test.com' in DB with id: " + id);
*/
            }
        }.run()
    }
}
