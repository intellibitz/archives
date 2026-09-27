package com.mobeegal.android.service;

import android.os.IBinder;
import android.os.RemoteException;

/**
 * a secondary interface associated with a service.  (Note that the interface
 * itself doesn't impact, it is just a matter of how you retrieve it from the
 * service.)
 */
public interface ISecondary
        extends android.os.IInterface
{
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub
            extends android.os.Binder
            implements com.mobeegal.android.service.ISecondary
    {
        private static final java.lang.String DESCRIPTOR =
                "com.mobeegal.android.service.ISecondary";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub()
        {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an ISecondary interface, generating a
         * proxy if needed.
         */
        public static com.mobeegal.android.service.ISecondary asInterface(
                android.os.IBinder obj)
        {
            if ((obj == null))
            {
                return null;
            }
            com.mobeegal.android.service.ISecondary in =
                    (com.mobeegal.android.service.ISecondary) obj
                            .queryLocalInterface(DESCRIPTOR);
            if ((in != null))
            {
                return in;
            }
            return new com.mobeegal.android.service.ISecondary.Stub.Proxy(obj);
        }

        public android.os.IBinder asBinder()
        {
            return this;
        }

        public boolean onTransact(int code, android.os.Parcel data,
                android.os.Parcel reply, int flags)
                throws RemoteException
        {
            try
            {
                switch (code)
                {
                    case TRANSACTION_getPid:
                    {
                        int _result = this.getPid();
                        reply.writeInt(_result);
                        return true;
                    }
                    case TRANSACTION_basicTypes:
                    {
                        int _arg0;
                        _arg0 = data.readInt();
                        long _arg1;
                        _arg1 = data.readLong();
                        boolean _arg2;
                        _arg2 = (0 != data.readInt());
                        float _arg3;
                        _arg3 = data.readFloat();
                        double _arg4;
                        _arg4 = data.readDouble();
                        java.lang.String _arg5;
                        _arg5 = data.readString();
                        this.basicTypes(_arg0, _arg1, _arg2, _arg3, _arg4,
                                _arg5);
                        return true;
                    }
                }
            }
            catch (android.os.DeadObjectException e)
            {
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy
                implements com.mobeegal.android.service.ISecondary
        {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote)
            {
                mRemote = remote;
            }

            public android.os.IBinder asBinder()
            {
                return mRemote;
            }

            /**
             * Request the PID of this service, to do evil things with it.
             */
            public int getPid()
                    throws android.os.DeadObjectException
            {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result = 0;
                try
                {
                    mRemote.transact(Stub.TRANSACTION_getPid, _data, _reply, 0);
                    _result = _reply.readInt();
                }
                catch (RemoteException e)
                {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                finally
                {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            /**
             * This demonstrates the basic types that you can use as parameters
             * and return values in AIDL.
             */
            public void basicTypes(int anInt, long aLong, boolean aBoolean,
                    float aFloat, double aDouble, java.lang.String aString)
                    throws android.os.DeadObjectException
            {
                android.os.Parcel _data = android.os.Parcel.obtain();
                try
                {
                    _data.writeInt(anInt);
                    _data.writeLong(aLong);
                    _data.writeInt(((aBoolean) ? (1) : (0)));
                    _data.writeFloat(aFloat);
                    _data.writeDouble(aDouble);
                    _data.writeString(aString);
                    mRemote.transact(TRANSACTION_basicTypes, _data, null,
                            0);
                }
                catch (RemoteException e)
                {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                finally
                {
                    _data.recycle();
                }
            }
        }

        static final int TRANSACTION_getPid =
                (IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_basicTypes =
                (IBinder.FIRST_CALL_TRANSACTION + 1);
    }

    /**
     * Request the PID of this service, to do evil things with it.
     */
    public int getPid()
            throws android.os.DeadObjectException;

    /**
     * This demonstrates the basic types that you can use as parameters and
     * return values in AIDL.
     */
    public void basicTypes(int anInt, long aLong, boolean aBoolean,
            float aFloat, double aDouble, java.lang.String aString)
            throws android.os.DeadObjectException;
}
