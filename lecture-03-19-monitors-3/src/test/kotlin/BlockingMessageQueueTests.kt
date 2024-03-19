import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


class BlockingMessageQueue<T>(private val capacity: Int) {

    @Throws(InterruptedException::class)
    fun tryEnqueue(messages: List<T>, timeout: Duration): Boolean {
        TODO()
    }

    @Throws(InterruptedException::class)
    fun tryDequeue(timeout: Duration): T? {
        TODO()
    }

}

class BlockingMessageQueueTests {
    /**
     * this simple test presents most of the considered best practices
     * used on concurrency tests construction
     *
     * - first, create the artifacts needed
     * - second, launch the thread(s) that executes the (potentially blocking) code to test
     * - start building simple tests that test single operations on a simple context
     * - the created thread(s) should not execute any assertion,
     *   they just create the needed result artifacts
     * - the test thread collects the  results, after joining the created threads(s)
     *   with an appropriate timeout to avoid running tests with undefinable times
     * - finally the test thread runs the necessary assertions to check the
     *   test success
     */
    @Test
    fun `blocking message single dequeue on an initial empty queue`() {
        // build the needed artifacts
        val capacity = 1
        val msgQueue = BlockingMessageQueue<String>(capacity)
        val expectedResult = "Ok"
        var result: String? = null

        // create a thread that run the code to test and produce the necessary results
        val consumer = thread {
            result = msgQueue.tryDequeue(1000.milliseconds)
        }

        // create a thread that run the code to test and produce the necessary results
        val producer = thread {
            msgQueue.tryEnqueue(listOf(expectedResult), Duration.ZERO)
        }

        // join the created threads e«with a timeout
        producer.join(5000)
        consumer.join(5000)

        // do the necessary assertions
        Assertions.assertEquals(expectedResult, result)
    }
}
