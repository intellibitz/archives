package com.mobeegal.android.service

import android.os.IBinder
import android.os.RemoteException

interface ICatalogService : android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    abstract class Stub : android.os.Binder(), ICatalogService {
        init {
            this.attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): android.os.IBinder {
            return this
        }

        @Throws(RemoteException::class)
        override fun onTransact(
            code: Int, data: android.os.Parcel,
            reply: android.os.Parcel?, flags: Int
        ): Boolean {
            try {
                when (code) {
                    TRANSACTION_registerCallback -> {
                        val _arg0 = ICatalogServiceCallback.Stub
                            .asInterface(data.readStrongBinder())
                        this.registerCallback(_arg0)
                        return true
                    }
                    TRANSACTION_unregisterCallback -> {
                        val _arg0 = ICatalogServiceCallback.Stub
                            .asInterface(data.readStrongBinder())
                        this.unregisterCallback(_arg0)
                        return true
                    }
                }
            } catch (e: android.os.DeadObjectException) {
            }
            return super.onTransact(code, data, reply, flags)
        }

        private class Proxy(private val mRemote: android.os.IBinder) : ICatalogService {

            override fun asBinder(): android.os.IBinder {
                return mRemote
            }

            /**
             * Often you want to allow a service to call back to its clients.
             * This shows how to do so, by registering a callback interface with
             * the service.
             */
            @Throws(android.os.DeadObjectException::class)
            override fun registerCallback(cb: ICatalogServiceCallback?) {
                val _data = android.os.Parcel.obtain()
                try {
                    _data.writeStrongBinder(
                        if (cb != null) cb.asBinder() else null
                    )
                    mRemote.transact(
                        TRANSACTION_registerCallback, _data,
                        null, 0
                    )
                } catch (e: RemoteException) {
                    e.printStackTrace() //To change body of catch statement use File | Settings | File Templates.
                } finally {
                    _data.recycle()
                }
            }

            /**
             * Remove a previously registered callback interface.
             */
            @Throws(android.os.DeadObjectException::class)
            override fun unregisterCallback(cb: ICatalogServiceCallback?) {
                val _data = android.os.Parcel.obtain()
                try {
                    _data.writeStrongBinder(
                        if (cb != null) cb.asBinder() else null
                    )
                    mRemote.transact(
                        TRANSACTION_unregisterCallback, _data,
                        null, 0
                    )
                } catch (e: RemoteException) {
                    e.printStackTrace() //To change body of catch statement use File | Settings | File Templates.
                } finally {
                    _data.recycle()
                }
            }
        }

        companion object {
            private const val DESCRIPTOR =
                "com.mobeegal.android.service.CatalogService"

            /**
             * Cast an IBinder object into an ICatalogService interface, generating
             * a proxy if needed.
             */
            @JvmStatic
            fun asInterface(obj: android.os.IBinder?): ICatalogService? {
                if (obj == null) {
                    return null
                }
                val `in` = obj
                    .queryLocalInterface(DESCRIPTOR) as ICatalogService?
                if (`in` != null) {
                    return `in`
                }
                return Proxy(obj) as ICatalogService
            }

            val TRANSACTION_registerCallback: Int =
                IBinder.FIRST_CALL_TRANSACTION + 0
            val TRANSACTION_unregisterCallback: Int =
                IBinder.FIRST_CALL_TRANSACTION + 1
        }
    }

    /**
     * Often you want to allow a service to call back to its clients. This shows
     * how to do so, by registering a callback interface with the service.
     */
    @Throws(android.os.DeadObjectException::class)
    fun registerCallback(cb: ICatalogServiceCallback?)

    /**
     * Remove a previously registered callback interface.
     */
    @Throws(android.os.DeadObjectException::class)
    fun unregisterCallback(cb: ICatalogServiceCallback?)
}
