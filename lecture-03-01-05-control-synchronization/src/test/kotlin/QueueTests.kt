import pt.isel.pc.controlsynchro.Queue
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals

class QueueTests {

    @org.junit.jupiter.api.Test
    fun `a single consumer and producer simple test`() {
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

    @org.junit.jupiter.api.Test
    fun `multiple use with a single consumer and producer test`() {
        val queue = Queue<Int>(10)
        val consumedValues = mutableSetOf<Int>()
        val NVALUES = 100_000

        val tconsumer = Thread {
            while(true) {
                val res = queue.get()
                if (res < 0) break
                consumedValues.add(res)
            }
        }
        tconsumer.start()

        val tproducer = Thread {
            repeat(NVALUES) {
                queue.put(it)
            }
            queue.put(-1)
        }
        tproducer.start()

        tproducer.join(3000)
        tconsumer.join(3000)

        assertEquals(NVALUES, consumedValues.size)
    }

}