package isel.leic.pc

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pt.isel.pc.client.EchoClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class EchoServerTest {
    companion object {
        val nclients = 10000
        private val logger = KotlinLogging.logger {}
    }

    @Test
    fun `check if no duplicate id's on multiple echo clients`() {
        // val ids = mutableSetOf<Int>() - bad choice, the set is not thread safe
        val ids = ConcurrentHashMap.newKeySet<Int>()
        val threads = mutableListOf<Thread>()
        repeat(nclients) {
             val thread =  Thread  {
                val client = EchoClient("127.0.0.1", 8080)
                val res = client.contact()
                if (!ids.add(res)) {
                    logger.warn("$res: duplicated id!")
                }
            }
            threads.add(thread)
            thread.start()
            if (it % 10 == 0) TimeUnit.MICROSECONDS.sleep(6000)
        }

        for(t in threads) t.join()

        assertEquals(nclients, ids.size)

    }
}