package pt.isel.pc.client

import mu.KotlinLogging
import pt.isel.pc.writeLine
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.measureTime

private val logger = KotlinLogging.logger {}

class EchoClient(private val serverAddress: String, private val serverPort : Int) {

    fun contact() : Int {
        var id : Int = 0
        try {
            val socket = Socket()
            socket.use {
                socket.connect(InetSocketAddress(serverAddress, serverPort))
                val reader = socket.getInputStream().bufferedReader()
                val writer = socket.getOutputStream().bufferedWriter()
                reader.use {
                    val line = reader.readLine()
                    val parts = line.split(" ")
                    writer.use {
                        writer.writeLine("bye")
                    }
                    id  = parts[2].toInt()
                }
            }
        }
        catch(e: IOException) {
            println("error on connect:${e.message}, ${e.cause?.message}")
        }
        return id
    }


}

fun main() {
    val nclients = 3000

    val time = measureTime {
        val ids = ConcurrentHashMap.newKeySet<Int>()
        val threads = mutableListOf<Thread>()
        repeat(nclients) {
            val thread = Thread {
                val client = EchoClient("127.0.0.1", 8080)
                val res = client.contact()

                if (!ids.add(res)) {
                    println("$res: duplicated id!")
                }

            }

            threads.add(thread)
            thread.start()
        }
        logger.info("wait for threads!")
        for (t in threads) t.join(5000)

        if (nclients != ids.size) {
            println("nclients=$nclients, ids.size=${ids.size}")
        } else {
            println("all ok!")
        }
    }
    println("done in $time ms!")

}