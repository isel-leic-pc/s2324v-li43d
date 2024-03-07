package pt.isel.pc.echoservers

import mu.KotlinLogging
import pt.isel.pc.writeLine
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Semaphore


fun main() {
    val server = EchoServerMT_Bounded("0.0.0.0", 8080)
    server.run()
}

class EchoServerMT_Bounded(private val address: String, private val port : Int = 8080) {

    companion object {
        private val logger = KotlinLogging.logger {}
        private val EXIT_CMD = "bye"
        private val MAX_SESSIONS = 20
    }

    private val sessionsAvaiable = Semaphore(MAX_SESSIONS)

    fun run( ) {
        var clientId : Int = 0
        val serverSock = ServerSocket( )

        // newClientId can't be declared here
        // since it will be shared by all client threads
        //var newClientId = 0
        serverSock.use {
            serverSock.bind(InetSocketAddress(address, port))
            logger.info("Server ready!")
            while(true) {
                sessionsAvaiable.acquire()
                val clientSock = serverSock.accept();

                // note the value newClientId must be local
                val newClientId = ++clientId
                //logger.info("connected with ${clientSock.remoteSocketAddress}")
                Thread {
                    // on first version the code was the next commented line
                    // processClient(clientSock, ++clientId )
                    // bad choice, not thread safe, incrementes can be lost,
                    // resulting in duplicated ids for clients
                    try {
                        processClient(clientSock, newClientId )

                    }
                    finally {
                        sessionsAvaiable.release()
                    }

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