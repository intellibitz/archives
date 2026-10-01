package intellibitz.intellidroid

import io.socket.client.IO
import io.socket.client.Socket
import org.junit.Test
import java.util.*

class SocketIOTest {
    @Test
    @Throws(Exception::class)
    fun socket_io() {
        // write your code here
        val socket: Socket
        socket = IO.socket("http://127.0.0.1:3010")
        socket.on(Socket.EVENT_CONNECT) {
            println("Connect.....")
            socket.emit("my other event", "968768")
            socket.emit("test1", "968768")
            socket.emit("test_send_msg", "This is from Android .. woohoooo")
            //                    socket.disconnect();
        }.on(Socket.EVENT_ERROR) {
            println("Error.....")
            // socket.emit("my other event", "968768");
//                    socket.disconnect();
        }.on(Socket.EVENT_DISCONNECT) {
            println("Disconnect.....")
            // socket.emit("my other event", "968768");
//                    socket.disconnect();
        }.on("test_new_msg") { args ->
            println("Listening....." + Arrays.toString(args))
            // socket.emit("my other event", "968768");
//                    socket.disconnect();
        }.on("test2") { args ->
            println("Listening....." + Arrays.toString(args))
            // socket.emit("my other event", "968768");
//                    socket.disconnect();
        }
//        socket.connect()
        //            Thread.sleep(5000);
    }
}