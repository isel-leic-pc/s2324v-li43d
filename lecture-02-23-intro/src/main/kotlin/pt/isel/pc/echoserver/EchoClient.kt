package pt.isel.pc.echoserver

import mu.KotlinLogging
import java.net.InetSocketAddress
import java.net.Socket



class EchoClient(private val serverAddress: String, private val serverPort : Int) {

    fun contact() : Int {
        val socket = Socket()
        socket.use {
            socket.connect(InetSocketAddress(serverAddress, serverPort))
            val reader = socket.getInputStream().bufferedReader()
            reader.use {
                val line = reader.readLine()
                val parts = line.split(" ")
                return parts[2].toInt()
            }
        }
    }
}