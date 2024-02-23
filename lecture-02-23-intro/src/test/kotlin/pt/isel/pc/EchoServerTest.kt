package pt.isel.pc

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import pt.isel.pc.echoserver.EchoClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread



class EchoServerTest {
    companion object {
        val nclients = 5000
        private val logger = KotlinLogging.logger {}
    }

    @Test
    fun `check if no duplicate id's on multiple echo clients`() {
        val ids = ConcurrentHashMap.newKeySet<Int>()
        val threads = mutableListOf<Thread>()
        repeat(nclients) {
            threads.add( Thread.ofVirtual().start {
                val client = EchoClient("127.0.0.1", 8080)
                val res = client.contact()

                if (!ids.add(res)) {
                    logger.warn("$res: duplicated id!")
                }
            })
            TimeUnit.NANOSECONDS.sleep(1)
        }

        for(t in threads) t.join()

        assertEquals(nclients, ids.size)

    }
}