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
 * DummyUserTest Unit test for the DummyUser Entity
 */
class DummyUserTest : EJBTest() {

    var log: Logger = Logger.getLogger("DummyUserTest")

    @Test
    @Throws(Exception::class)
    fun testDummyUser() {
        object : ComponentTest() {
            @Throws(Exception::class)
            override fun testComponents() {
                // This is a transactionally aware EntityManager and must be accessed within a JTA transaction
                // Why aren't we using javax.persistence.Persistence?  Well, our persistence.xml file uses
                // jta-datasource which means that it is created by the EJB container/embedded JBoss.
                // using javax.persistence.Persistence will just cause us an error
                val em = InitialContext().lookup(
                    "java:/EntityManagers/DefaultDS"
                ) as EntityManager
//                    getInstance("DefaultDS");

                // Obtain JBoss transaction
                val tm = InitialContext().lookup(
                    "java:/TransactionManager"
                ) as TransactionManager

                tm.begin()

                var dummyUser = DummyUser()
                dummyUser.name = "test1"
                dummyUser.username = "test1"
                dummyUser.password = "test1"
                em.persist(dummyUser)

                assert(dummyUser.id > 0)

                tm.commit()
                val id = dummyUser.id
                log.info("created user 'test1' in DB with id: $id")

                tm.begin()
                dummyUser = em.find(DummyUser::class.java, id)
                assert(null != dummyUser)
                tm.commit()
                log.info("found user 'test1' in DB with id: $id")
            }
        }.run()
    }
}
