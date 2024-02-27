package pt.isel.pc.echoservers

import mu.KotlinLogging
import pt.isel.pc.writeLine
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.TimeUnit


fun main() {
    val server = EchoServerMT("0.0.0.0", 8080)
    server.run()
}

class EchoServerMT(private val address: String, private val port : Int = 8080) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val EXIT_CMD = "bye"

    }

    fun run( ) {
        var clientId : Int = 0
        val serverSock = ServerSocket( )
        //var newClientId = 0
        serverSock.use {
            serverSock.bind(InetSocketAddress(address, port))
            logger.info("Server ready!")
            while(true) {
                val clientSock = serverSock.accept();
                val newClientId = ++clientId
                //logger.info("connected with ${clientSock.remoteSocketAddress}")
                Thread {
                    processClient(clientSock, newClientId )
                }.start()
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