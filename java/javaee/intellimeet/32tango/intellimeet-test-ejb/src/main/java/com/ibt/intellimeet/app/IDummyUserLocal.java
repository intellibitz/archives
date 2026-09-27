package com.ibt.intellimeet.app;

/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
$HeadURL::                                                                      $: Head URL of last commit
-->
*/

import com.ibt.intellimeet.data.DummyUser;

import javax.ejb.Local;

/**
 * Created by IntelliJ IDEA. User: sara Date: Dec 15, 2007 Time: 12:40:14 PM To
 * change this template use File | Settings | File Templates.
 */
@Local
public interface IDummyUserLocal
{
    public DummyUser findDummyUser(long id);

    public long register();

    public void invalid();

    public String getVerify();

    public void setVerify(String verify);

    public boolean isRegistered();

    public void destroy();

    public void run();

    public void cancel();

    public DummyUser getDummyUser();

    public void setDummyUser(DummyUser dummyUser);
}
