import pt.isel.pc.controlsynchro.Queue
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals

class QueueTests {

    @Test
    fun `simple test with a consumer and a producer`() {
        val queue = Queue<Int>(1)
        var result : Int = 0

        val tconsumer = thread {
            result = queue.get()
        }

        val tproducer = thread {
            Thread.sleep(1000)
            queue.put(2)
        }

        tproducer.join(3000)
        tconsumer.join(3000)

        assertEquals(2, result)
    }
}