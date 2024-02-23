package pt.isel.pc.echoserver

import mu.KotlinLogging
import pt.isel.pc.writeLine
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

private fun main() {
    val server = EchoServer0("0.0.0.0", 8080)
    server.run()
}

class EchoServer0(private val address: String, private val port : Int = 8080) {
    private var clientId : Int = 0
    companion object {
        private val logger = KotlinLogging.logger {}
        private val EXIT_CMD = "bye"
    }

    fun run( ) {
        val serverSock = ServerSocket()
        serverSock.use {
            serverSock.bind(InetSocketAddress(address, port));
            while(true) {
                val clientSock = serverSock.accept();
                //logger.info("connected with ${clientSock.remoteSocketAddress}")

                val t = Thread {

                    processClient(clientSock, ++clientId )
                }
                t.start()

            }
        }
    }

    private fun processClient(clientSock: Socket, clientNum : Int) {
        clientSock.use {
            val reader =  clientSock.getInputStream().bufferedReader()
            val writer =  clientSock.getOutputStream().bufferedWriter()

            reader.use {
                writer.use {
                    writer.writeLine("Hello, client $clientNum")
                    do {
                        val line = reader.readLine()
                        if (line == null || line == EXIT_CMD) {
                            writer.writeLine("Bye, client $clientNum")
                            break
                        }
                        else {
                            writer.writeLine("Echoing '$line'")
                        }
                    }
                    while(true)
                }
            }
        }
    }
}