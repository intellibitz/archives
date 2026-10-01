package com.mobeegal.android.service

import android.os.IBinder
import android.os.RemoteException

/**
 * a secondary interface associated with a service.  (Note that the interface
 * itself doesn't impact, it is just a matter of how you retrieve it from the
 * service.)
 */
interface ISecondary : android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    abstract class Stub : android.os.Binder(), ISecondary {
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
                    TRANSACTION_getPid -> {
                        val _result = this.getPid()
                        reply!!.writeInt(_result)
                        return true
                    }
                    TRANSACTION_basicTypes -> {
                        val _arg0 = data.readInt()
                        val _arg1 = data.readLong()
                        val _arg2 = 0 != data.readInt()
                        val _arg3 = data.readFloat()
                        val _arg4 = data.readDouble()
                        val _arg5 = data.readString()
                        this.basicTypes(
                            _arg0, _arg1, _arg2, _arg3, _arg4,
                            _arg5
                        )
                        return true
                    }
                }
            } catch (e: android.os.DeadObjectException) {
            }
            return super.onTransact(code, data, reply, flags)
        }

        private class Proxy(private val mRemote: android.os.IBinder) : ISecondary {

            override fun asBinder(): android.os.IBinder {
                return mRemote
            }

            /**
             * Request the PID of this service, to do evil things with it.
             */
            @Throws(android.os.DeadObjectException::class)
            override fun getPid(): Int {
                val _data = android.os.Parcel.obtain()
                val _reply = android.os.Parcel.obtain()
                var _result = 0
                try {
                    mRemote.transact(TRANSACTION_getPid, _data, _reply, 0)
                    _result = _reply.readInt()
                } catch (e: RemoteException) {
                    e.printStackTrace() //To change body of catch statement use File | Settings | File Templates.
                } finally {
                    _reply.recycle()
                    _data.recycle()
                }
                return _result
            }

            /**
             * This demonstrates the basic types that you can use as parameters
             * and return values in AIDL.
             */
            @Throws(android.os.DeadObjectException::class)
            override fun basicTypes(
                anInt: Int, aLong: Long, aBoolean: Boolean,
                aFloat: Float, aDouble: Double, aString: String?
            ) {
                val _data = android.os.Parcel.obtain()
                try {
                    _data.writeInt(anInt)
                    _data.writeLong(aLong)
                    _data.writeInt(if (aBoolean) 1 else 0)
                    _data.writeFloat(aFloat)
                    _data.writeDouble(aDouble)
                    _data.writeString(aString)
                    mRemote.transact(TRANSACTION_basicTypes, _data, null, 0)
                } catch (e: RemoteException) {
                    e.printStackTrace() //To change body of catch statement use File | Settings | File Templates.
                } finally {
                    _data.recycle()
                }
            }
        }

        companion object {
            private const val DESCRIPTOR =
                "com.mobeegal.android.service.ISecondary"

            /**
             * Cast an IBinder object into an ISecondary interface, generating a
             * proxy if needed.
             */
            @JvmStatic
            fun asInterface(obj: android.os.IBinder?): ISecondary? {
                if (obj == null) {
                    return null
                }
                val `in` = obj
                    .queryLocalInterface(DESCRIPTOR) as ISecondary?
                if (`in` != null) {
                    return `in`
                }
                return Proxy(obj)
            }

            val TRANSACTION_getPid: Int =
                IBinder.FIRST_CALL_TRANSACTION + 0
            val TRANSACTION_basicTypes: Int =
                IBinder.FIRST_CALL_TRANSACTION + 1
        }
    }

    /**
     * Request the PID of this service, to do evil things with it.
     */
    @Throws(android.os.DeadObjectException::class)
    fun getPid(): Int

    /**
     * This demonstrates the basic types that you can use as parameters and
     * return values in AIDL.
     */
    @Throws(android.os.DeadObjectException::class)
    fun basicTypes(
        anInt: Int, aLong: Long, aBoolean: Boolean,
        aFloat: Float, aDouble: Double, aString: String?
    )
}
