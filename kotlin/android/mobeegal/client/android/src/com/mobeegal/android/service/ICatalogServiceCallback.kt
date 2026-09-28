package com.mobeegal.android.service

import android.os.IBinder
import android.os.RemoteException

/**
 * callback interface used by IRemoteService to send synchronous notifications
 * back to its clients.
 */
interface ICatalogServiceCallback : android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    abstract class Stub : android.os.Binder(), ICatalogServiceCallback {
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
                    TRANSACTION_valueChanged -> {
                        val _arg0 = data.readInt()
                        this.valueChanged(_arg0)
                        return true
                    }
                }
            } catch (e: android.os.DeadObjectException) {
            }
            return super.onTransact(code, data, reply, flags)
        }

        private class Proxy(private val mRemote: android.os.IBinder) : ICatalogServiceCallback {

            override fun asBinder(): android.os.IBinder {
                return mRemote
            }

            /**
             * Called when the service has a new value for you.
             */
            @Throws(android.os.DeadObjectException::class)
            override fun valueChanged(value: Int) {
                val _data = android.os.Parcel.obtain()
                try {
                    _data.writeInt(value)
                    mRemote.transact(TRANSACTION_valueChanged, _data, null, 0)
                } catch (e: RemoteException) {
                    e.printStackTrace() //To change body of catch statement use File | Settings | File Templates.
                } finally {
                    _data.recycle()
                }
            }
        }

        companion object {
            private const val DESCRIPTOR =
                "com.mobeegal.android.service.ICatalogServiceCallback"

            /**
             * Cast an IBinder object into an ICatalogServiceCallback interface,
             * generating a proxy if needed.
             */
            @JvmStatic
            fun asInterface(obj: android.os.IBinder?): ICatalogServiceCallback? {
                if (obj == null) {
                    return null
                }
                val `in` = obj
                    .queryLocalInterface(DESCRIPTOR) as ICatalogServiceCallback?
                if (`in` != null) {
                    return `in`
                }
                return Proxy(obj)
            }

            val TRANSACTION_valueChanged: Int =
                IBinder.FIRST_CALL_TRANSACTION + 0
        }
    }

    /**
     * Called when the service has a new value for you.
     */
    @Throws(android.os.DeadObjectException::class)
    fun valueChanged(value: Int)
}
