package com.ibt.intellimeet.ejb

/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
$HeadURL::                                                                      $: Head URL of last commit
-->
*/

import com.ibt.intellimeet.app.DummyUserAction
import com.ibt.intellimeet.app.IDummyUserLocal
import com.ibt.intellimeet.data.DummyUser
import org.jboss.deployers.spi.DeploymentException
import org.jboss.embedded.Bootstrap
import org.jboss.seam.mock.SeamTest
import org.jboss.virtual.plugins.context.vfs.AssembledContextFactory
import org.jboss.virtual.plugins.context.vfs.AssembledDirectory
import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeSuite
import java.util.logging.Logger

/**
 * EJBTest - Base Unit test for the Entity Beans
 */
open class EJBTest : SeamTest() {

    var log: Logger = Logger.getLogger("EJBTest")

    @Throws(Exception::class)
    override fun startJbossEmbeddedIfNecessary() {
    }

    @BeforeSuite
    @Throws(Exception::class)
    protected fun setUp() {
        if (globalSetup) {
            return
        }
        Bootstrap.getInstance().bootstrap()
        deploy()
        globalSetup = true
        log.info("Embedded Jboss deploy - SUCCESS")
    }

    @AfterSuite
    @Throws(Exception::class)
    protected fun tearDown() {
        if (!globalSetup) {
            return
        }
        undeploy()
        globalSetup = false
        log.info("Embedded Jboss undeploy - SUCCESS")
    }

    companion object {
        private var globalSetup = false
        private var jar: AssembledDirectory? = null

        private fun deploy() {
            jar = AssembledContextFactory.getInstance()
                .create("intellimeet-test-ejb.jar")
            jar!!.addClass(DummyUser::class.java)
            jar!!.addClass(IDummyUserLocal::class.java)
            jar!!.addClass(DummyUserAction::class.java)
            val assembledDirectory = jar!!.mkdir("META-INF")
            assembledDirectory.addResource("META-INF/persistence.xml")
            assembledDirectory.addResource("META-INF/components.xml")
            try {
                val bootstrap = Bootstrap.getInstance()
                if (resourceExists("META-INF/components.xml")) {
                    bootstrap.deployResourceBases("META-INF/components.xml")
                }
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
    }
}
