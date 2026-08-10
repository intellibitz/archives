package cc.thevar.blukit.data.bluetooth

import android.bluetooth.BluetoothSocket
import cc.thevar.blukit.data.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class BluetoothDataTransferService(
    private val socket: BluetoothSocket,
    private val cryptoManager: CryptoManager = CryptoManager()
) {
    private val inputStream = DataInputStream(socket.inputStream)
    private val outputStream = DataOutputStream(socket.outputStream)

    fun listenForIncomingPayloads(): Flow<BluetoothPayload> {
        return flow<BluetoothPayload> {
            if (!socket.isConnected) {
                return@flow
            }
            while (true) {
                val byteCount = try {
                    inputStream.readInt()
                } catch (e: IOException) {
                    throw IOException("Reading from input stream failed")
                }

                val buffer = ByteArray(byteCount)
                try {
                    inputStream.readFully(buffer)
                } catch (e: IOException) {
                    throw IOException("Reading payload failed")
                }

                val decryptedBytes = try {
                    cryptoManager.decrypt(buffer)
                } catch (e: Exception) {
                    continue
                }

                val payloadJson = decryptedBytes.decodeToString()
                val payload = Json.decodeFromString<BluetoothPayload>(payloadJson)
                emit(payload)
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendPayload(payload: BluetoothPayload): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val payloadJson = Json.encodeToString(payload)
                val encryptedBytes = cryptoManager.encrypt(payloadJson.toByteArray())
                
                outputStream.writeInt(encryptedBytes.size)
                outputStream.write(encryptedBytes)
                outputStream.flush()
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }
}
