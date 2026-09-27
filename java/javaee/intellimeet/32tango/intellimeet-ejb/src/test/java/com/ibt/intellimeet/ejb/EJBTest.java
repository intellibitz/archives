package com.ibt.intellimeet.ejb;

/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
$HeadURL::                                                                      $: Head URL of last commit
-->
*/

import com.ibt.intellimeet.data.User;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.embedded.Bootstrap;
import org.jboss.seam.mock.SeamTest;
import org.jboss.virtual.plugins.context.vfs.AssembledContextFactory;
import org.jboss.virtual.plugins.context.vfs.AssembledDirectory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.util.logging.Logger;

/**
 * EJBTest - Base Unit test for the Entity Beans
 */
public class EJBTest
        extends SeamTest
{

    private static boolean globalSetup = false;
    Logger log = Logger.getLogger("EJBTest");

    private static AssembledDirectory jar;

    public EJBTest()
    {
        super();
    }

    @Override
    protected void startJbossEmbeddedIfNecessary()
            throws Exception
    {
    }

    private static void deploy()
    {
        jar = AssembledContextFactory.getInstance()
                .create("intellimeet-ejb.jar");
        jar.addClass(User.class);
        AssembledDirectory assembledDirectory = jar.mkdir("META-INF");
        assembledDirectory.addResource("META-INF/persistence.xml");
        assembledDirectory.addResource("META-INF/components.xml");
        try
        {
            Bootstrap bootstrap = Bootstrap.getInstance();
            if (resourceExists("META-INF/components.xml"))
            {
                bootstrap.deployResourceBases("META-INF/components.xml");
            }
            bootstrap.deploy(jar);
        }
        catch (DeploymentException e)
        {
            throw new RuntimeException("Unable to deploy", e);
        }
    }


    private static boolean resourceExists(String name)
    {
        return Thread.currentThread().getContextClassLoader()
                .getResource(name) != null;
    }

    private static void undeploy()
    {
        try
        {
            Bootstrap.getInstance().undeploy(jar);
            AssembledContextFactory.getInstance().remove(jar);
        }
        catch (DeploymentException e)
        {
            throw new RuntimeException("Unable to undeploy", e);
        }
    }

    @BeforeSuite
    protected void setUp()
            throws Exception
    {
        if (globalSetup)
        {
            return;
        }
        Bootstrap.getInstance().bootstrap();
        deploy();
        globalSetup = true;
        log.info("Embedded Jboss deploy - SUCCESS");
    }

    @AfterSuite
    protected void tearDown()
            throws Exception
    {
        if (!globalSetup)
        {
            return;
        }
        undeploy();
        globalSetup = false;
        log.info("Embedded Jboss undeploy - SUCCESS");
    }

}