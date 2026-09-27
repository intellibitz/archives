package com.mobeegal.android.service;

import android.os.IBinder;
import android.os.RemoteException;

/**
 * callback interface used by IRemoteService to send synchronous notifications
 * back to its clients.
 */
public interface ICatalogServiceCallback
        extends android.os.IInterface
{
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub
            extends android.os.Binder
            implements com.mobeegal.android.service.ICatalogServiceCallback
    {
        private static final java.lang.String DESCRIPTOR =
                "com.mobeegal.android.service.ICatalogServiceCallback";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub()
        {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an ICatalogServiceCallback interface,
         * generating a proxy if needed.
         */
        public static com.mobeegal.android.service.ICatalogServiceCallback asInterface(
                android.os.IBinder obj)
        {
            if ((obj == null))
            {
                return null;
            }
            com.mobeegal.android.service.ICatalogServiceCallback in =
                    (com.mobeegal.android.service.ICatalogServiceCallback) obj
                            .queryLocalInterface(DESCRIPTOR);
            if ((in != null))
            {
                return in;
            }
            return new com.mobeegal.android.service.ICatalogServiceCallback.Stub.Proxy(
                    obj);
        }

        public android.os.IBinder asBinder()
        {
            return this;
        }

        @Override
        public boolean onTransact(int code, android.os.Parcel data,
                android.os.Parcel reply, int flags)
                throws RemoteException
        {
            try
            {
                switch (code)
                {
                    case TRANSACTION_valueChanged:
                    {
                        int _arg0;
                        _arg0 = data.readInt();
                        this.valueChanged(_arg0);
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
                implements com.mobeegal.android.service.ICatalogServiceCallback
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
             * Called when the service has a new value for you.
             */
            public void valueChanged(int value)
                    throws android.os.DeadObjectException
            {
                android.os.Parcel _data = android.os.Parcel.obtain();
                try
                {
                    _data.writeInt(value);
                    mRemote.transact(Stub.TRANSACTION_valueChanged, _data, null,
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

        static final int TRANSACTION_valueChanged =
                (IBinder.FIRST_CALL_TRANSACTION + 0);
    }

    /**
     * Called when the service has a new value for you.
     */
    public void valueChanged(int value)
            throws android.os.DeadObjectException;
}
