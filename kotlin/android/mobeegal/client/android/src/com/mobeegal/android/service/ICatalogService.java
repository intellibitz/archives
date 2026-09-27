package com.mobeegal.android.service;


import android.os.IBinder;
import android.os.RemoteException;

public interface ICatalogService
        extends android.os.IInterface
{
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub
            extends android.os.Binder
            implements com.mobeegal.android.service.ICatalogService
    {
        private static final java.lang.String DESCRIPTOR =
                "com.mobeegal.android.service.CatalogService";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub()
        {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an ICatalogService interface, generating
         * a proxy if needed.
         */
        public static com.mobeegal.android.service.ICatalogService asInterface(
                android.os.IBinder obj)
        {
            if ((obj == null))
            {
                return null;
            }
            com.mobeegal.android.service.ICatalogService in =
                    (com.mobeegal.android.service.ICatalogService) obj
                            .queryLocalInterface(DESCRIPTOR);
            if ((in != null))
            {
                return in;
            }
            return (ICatalogService) new com.mobeegal.android.service.ICatalogService.Stub.Proxy(
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
                    case TRANSACTION_registerCallback:
                    {
                        com.mobeegal.android.service.ICatalogServiceCallback _arg0;
                        _arg0 = com.mobeegal.android.service
                                .ICatalogServiceCallback.Stub
                                .asInterface(data.readStrongBinder());
                        this.registerCallback(_arg0);
                        return true;
                    }
                    case TRANSACTION_unregisterCallback:
                    {
                        com.mobeegal.android.service.ICatalogServiceCallback _arg0;
                        _arg0 = com.mobeegal.android.service
                                .ICatalogServiceCallback.Stub
                                .asInterface(data.readStrongBinder());
                        this.unregisterCallback(_arg0);
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
                implements com.mobeegal.android.service.ICatalogService
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
             * Often you want to allow a service to call back to its clients.
             * This shows how to do so, by registering a callback interface with
             * the service.
             */
            public void registerCallback(
                    com.mobeegal.android.service.ICatalogServiceCallback cb)
                    throws android.os.DeadObjectException
            {
                android.os.Parcel _data = android.os.Parcel.obtain();
                try
                {
                    _data.writeStrongBinder(
                            (((cb != null)) ? (cb.asBinder()) : (null)));
                    mRemote.transact(TRANSACTION_registerCallback, _data,
                            null, 0);
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

            /**
             * Remove a previously registered callback interface.
             */
            public void unregisterCallback(
                    com.mobeegal.android.service.ICatalogServiceCallback cb)
                    throws android.os.DeadObjectException
            {
                android.os.Parcel _data = android.os.Parcel.obtain();
                try
                {
                    _data.writeStrongBinder(
                            (((cb != null)) ? (cb.asBinder()) : (null)));
                    mRemote.transact(TRANSACTION_unregisterCallback, _data,
                            null, 0);
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

        static final int TRANSACTION_registerCallback =
                (IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_unregisterCallback =
                (IBinder.FIRST_CALL_TRANSACTION + 1);
    }

    /**
     * Often you want to allow a service to call back to its clients. This shows
     * how to do so, by registering a callback interface with the service.
     */
    public void registerCallback(
            com.mobeegal.android.service.ICatalogServiceCallback cb)
            throws android.os.DeadObjectException;

    /**
     * Remove a previously registered callback interface.
     */
    public void unregisterCallback(
            com.mobeegal.android.service.ICatalogServiceCallback cb)
            throws android.os.DeadObjectException;
}
