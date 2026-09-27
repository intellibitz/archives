package com.ibt.intellidocs.dummy;

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
