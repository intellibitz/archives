package com.ibt.intellidocs.dummy

import org.jboss.deployers.spi.DeploymentException
import org.jboss.embedded.Bootstrap
import org.jboss.seam.mock.SeamTest
import org.jboss.virtual.plugins.context.vfs.AssembledContextFactory
import org.jboss.virtual.plugins.context.vfs.AssembledDirectory
import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeSuite
import org.testng.annotations.Test
import java.util.logging.Logger
import javax.naming.InitialContext
import javax.persistence.EntityManager
import javax.transaction.TransactionManager

/*
import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.extensions.TestSetup;
*/

/**
 * DummyUserTest Unit test for the DummyUser Entity
 */
class DummyUserTest : SeamTest() {

    @Throws(Exception::class)
    override fun startJbossEmbeddedIfNecessary() {
    }

    var log: Logger = Logger.getLogger("DummyUserTest")

    //    @Override

    @BeforeSuite
    @Throws(Exception::class)
    protected fun setUp() {
        if (globalSetup) {
            return
        }
        Bootstrap.getInstance().bootstrap()
        deploy()
        log.info("Embedded Jboss deploy - SUCCESS")
    }

    //    @Override
    @AfterSuite
    @Throws(Exception::class)
    protected fun tearDown() {
        if (globalSetup) {
            return
        }
        undeploy()
        log.info("Embedded Jboss undeploy - SUCCESS")
    }

/*
    @Configuration (beforeSuite = true)
    protected void setUp() throws Exception
    {
       if (globalSetup) return;
       Bootstrap.getInstance().bootstrap();
       deploy();
    }

    @Configuration (afterSuite = true)
    protected void tearDown() throws Exception
    {
        if (globalSetup) return;
        undeploy();
    }
*/

    @Test
    @Throws(Exception::class)
    fun testEntityManager() {
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

    companion object {
        private var jar: AssembledDirectory? = null
        private var globalSetup = false

        private fun deploy() {
            jar = AssembledContextFactory.getInstance()
                .create("intellidocs-ejb.jar")
            jar!!.addClass(DummyUser::class.java)
            jar!!.addClass(IDummyUserLocal::class.java)
            jar!!.addClass(DummyUserAction::class.java)
            val assembledDirectory = jar!!.mkdir("META-INF")
            assembledDirectory.addResource("META-INF/persistence.xml")
            assembledDirectory.addResource("META-INF/components.xml")
            try {
                val bootstrap = Bootstrap.getInstance()
/*
            if (resourceExists("seam.properties")) {
                bootstrap.deployResourceBases("seam.properties");
            }
*/
                if (resourceExists("META-INF/components.xml")) {
                    bootstrap.deployResourceBases("META-INF/components.xml")
                }
/*
            if (resourceExists("META-INF/seam.properties")) {
                bootstrap.deployResourceBases("META-INF/seam.properties");
            }
*/
                bootstrap.deploy(jar)
            } catch (e: DeploymentException) {
                throw RuntimeException("Unable to deploy", e)
            }
        }

        private fun resourceExists(name: String): Boolean {
            return Thread.currentThread().contextClassLoader
                .getResource(name) != null
        }

        private fun undeploy() {
            try {
                Bootstrap.getInstance().undeploy(jar)
                AssembledContextFactory.getInstance().remove(jar)
            } catch (e: DeploymentException) {
                throw RuntimeException("Unable to undeploy", e)
            }
        }

/*
    public static junit.framework.Test suite()
    {
       TestSuite suite = new TestSuite();
       suite.addTestSuite(DummyUserTest.class);
       globalSetup = true;

       return new TestSetup(suite)
       {
          @Override
          protected void setUp() throws Exception
          {
             super.setUp();
             if (!Bootstrap.getInstance().isStarted())
             {
                Bootstrap.getInstance().bootstrap();
             }
             deploy();
          }

          @Override
          protected void tearDown() throws Exception
          {
             undeploy();
             if (System.getProperty("shutdown.embedded.jboss") != null) Bootstrap.getInstance().shutdown();
             super.tearDown();
          }
       };
    }
*/
    }
}
