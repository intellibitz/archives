package com.ibt.intellidocs.dummy;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.embedded.Bootstrap;
import org.jboss.seam.mock.SeamTest;
import org.jboss.virtual.plugins.context.vfs.AssembledContextFactory;
import org.jboss.virtual.plugins.context.vfs.AssembledDirectory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;
import java.util.logging.Logger;

/*
import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.extensions.TestSetup;
*/


/**
 * DummyUserTest Unit test for the DummyUser Entity
 */
public class DummyUserTest
        extends SeamTest
{

    public DummyUserTest()
    {
        super();
    }

    @Override
    protected void startJbossEmbeddedIfNecessary()
            throws Exception
    {
    }

    Logger log = Logger.getLogger("DummyUserTest");

    private static AssembledDirectory jar;
    private static boolean globalSetup = false;

    private static void deploy()
    {
        jar = AssembledContextFactory.getInstance()
                .create("intellidocs-ejb.jar");
        jar.addClass(DummyUser.class);
        jar.addClass(IDummyUserLocal.class);
        jar.addClass(DummyUserAction.class);
        AssembledDirectory assembledDirectory = jar.mkdir("META-INF");
        assembledDirectory.addResource("META-INF/persistence.xml");
        assembledDirectory.addResource("META-INF/components.xml");
        try
        {
            Bootstrap bootstrap = Bootstrap.getInstance();
/*
            if (resourceExists("seam.properties")) {
                bootstrap.deployResourceBases("seam.properties");
            }
*/
            if (resourceExists("META-INF/components.xml"))
            {
                bootstrap.deployResourceBases("META-INF/components.xml");
            }
/*
            if (resourceExists("META-INF/seam.properties")) {
                bootstrap.deployResourceBases("META-INF/seam.properties");
            }
*/
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

    //    @Override

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
        log.info("Embedded Jboss deploy - SUCCESS");
    }

    //    @Override
    @AfterSuite
    protected void tearDown()
            throws Exception
    {
        if (globalSetup)
        {
            return;
        }
        undeploy();
        log.info("Embedded Jboss undeploy - SUCCESS");
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
    public void testEntityManager()
            throws Exception
    {

        new ComponentTest()
        {

            protected void testComponents()
                    throws Exception
            {
                // This is a transactionally aware EntityManager and must be accessed within a JTA transaction
                // Why aren't we using javax.persistence.Persistence?  Well, our persistence.xml file uses
                // jta-datasource which means that it is created by the EJB container/embedded JBoss.
                // using javax.persistence.Persistence will just cause us an error
                EntityManager em = (EntityManager)
                        new InitialContext().lookup
                                ("java:/EntityManagers/DefaultDS");
//                    getInstance("DefaultDS");

                // Obtain JBoss transaction
                TransactionManager tm =
                        (TransactionManager) new InitialContext().lookup
                                ("java:/TransactionManager");

                tm.begin();

                DummyUser dummyUser = new DummyUser();
                dummyUser.setName("test1");
                dummyUser.setUsername("test1");
                dummyUser.setPassword("test1");
                em.persist(dummyUser);

                assert (dummyUser.getId() > 0);

                tm.commit();
                long id = dummyUser.getId();
                log.info("created user 'test1' in DB with id: " + id);

                tm.begin();
                dummyUser = em.find(DummyUser.class, id);
                assert (null != dummyUser);
                tm.commit();
                log.info("found user 'test1' in DB with id: " + id);
            }
        }.run();

    }

}
