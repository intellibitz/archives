package com.ibt.intellimeet.data

import com.ibt.intellimeet.ejb.EJBTest
import java.util.logging.Logger
import org.testng.annotations.Test

/*
<!--
$Id::                                                                            $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
$HeadURL::                                                                      $: Head URL of last commit
-->
*/

/**
 * GroovyUserTest Unit test for the GroovyUser Entity
 */
class GroovyUserTest
extends EJBTest
{

    transient Logger log = Logger.getLogger("GroovyUserTest");

    public GroovyUserTest()
    {
        super();
    }

    @Test
    public void testGroovyUser()
    {
        log.info "todo: test needs to be implemented"
    }


}

