package com.ibt.intellimeet.app

/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
$HeadURL::                                                                      $: Head URL of last commit
-->
*/

import com.ibt.intellimeet.data.DummyUser
import javax.ejb.Local

/**
 * Created by IntelliJ IDEA. User: sara Date: Dec 15, 2007 Time: 12:40:14 PM To
 * change this template use File | Settings | File Templates.
 */
@Local
interface IDummyUserLocal {
    fun findDummyUser(id: Long): DummyUser?
    fun register(): Long
    fun invalid()
    fun getVerify(): String?
    fun setVerify(verify: String?)
    fun isRegistered(): Boolean
    fun destroy()
    fun run()
    fun cancel()
    fun getDummyUser(): DummyUser?
    fun setDummyUser(dummyUser: DummyUser?)
}
