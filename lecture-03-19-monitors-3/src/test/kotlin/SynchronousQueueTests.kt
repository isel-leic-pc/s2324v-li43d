import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import pt.isel.pc.monitors.SynchronousQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.test.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SynchronousQueueTests {
    private val logger = KotlinLogging.logger {}

    @Test
    fun `simple rendezvous deliver with SinchronousQueue`() {
        val squeue = SynchronousQueue<Int>()
        var result: Int? = null

        val poller = thread {
            result = squeue.poll()
        }

        val offeror = thread {
            squeue.offer(2)
        }

        offeror.join(2000)
        poller.join(2000)

        Assertions.assertNotNull(result)
        Assertions.assertEquals(2, result)
    }


    @Test
    fun `simple rendezvous deliver with SinchronousQueueMultiOffer`() {
        val squeue = SynchronousQueueMultiPoll<Int>()

        val expected = listOf(2)
        var result: List<Int>? = null

        val poller = thread {
            result = squeue.poll(1)
        }

        val offeror = thread {
            squeue.offer(2)
        }

        offeror.join(2000)
        poller.join(2000)

        Assertions.assertNotNull(result)
        Assertions.assertEquals(expected, result)
    }

    /**
     * A more complicated test that should be done after the simple tests
     * already succeed
     * In this case we have an arbitrary number of enqueuers and dequeuers
     * the test parametrizes the number of each.
     * Initially it should be parametrized to the most simple situation, NREADERS=NWRITERS=1
     * Varying capacity can test scenarios where enqueuers need to block
     * Note that this code could be refactored to an auxiliary method that
     * could be used to build tests for different scenarios.
     * Something like this:
     *   private fun <T>  exec_bmq_test(nwriters : Int = 1,
     *                                  nreaders : Int = 1,
     *                                  capacity : Int = 1,
     *                                  colSupplier : () -> Collection<T>)
     *                                      : Collection<T>
     */
    /*
    @Test
    fun blocking_synchronous_queue_multi_poll_with_multiple_senders_receivers() {

        val NWRITERS = 4
        val NREADERS = 2


        val listOfLists = listOf(
            listOf(1, 2, 3),
            listOf(4, 5),
            listOf(6),
            listOf(7, 8, 9, 10),
            listOf(11, 12, 13),
            listOf(14, 15, 16, 17),
            listOf(18, 19, 20)
        )


        val data_size =
            listOfLists
                .flatMap { it }
                .count()

        val msgQueue = SynchronousQueueMultiPoll<Int>()
        val resultSet = ConcurrentHashMap.newKeySet<Int>()

        // this index use is protected by a mutex
        // in oprder to support multiple writers
        var writeInputIndex = 0
        val mutex = ReentrantLock()

        val expectedSet = (1..20).toSet()

        val writerThreads = (1..NWRITERS)
            .map {
                thread {
                    logger?.info("start writer")
                    while (true) {
                        var localIdx = listOfLists.size
                        mutex.withLock {
                            if (writeInputIndex < listOfLists.size) {
                                localIdx = writeInputIndex++
                            }
                        }
                        if (localIdx < listOfLists.size) {
                            val value = listOfLists.get(localIdx)
                            logger?.info("writer try send $value")
                            if (msgQueue.offer(value, Duration.INFINITE))
                                logger?.info("writer send $value")
                        } else {
                            break;
                        }

                    }
                    logger?.info("end writer")
                }
            }

        val readerThreads = (1..NREADERS).map {
            thread {
                logger?.info("start reader")
                while (true) {
                    val value =
                        msgQueue.poll(3000.milliseconds) ?: break
                    if (resultSet.add(value))
                        logger?.info("reader get $value")
                }
                logger?.info("end reader")
            }
        }


        // wait for writers termination (with a timeout)
        for (wt in writerThreads) {
            wt.join(5000)
            if (wt.isAlive) {
                fail("too much execution time for writer thread")
            }
        }

        // wait for readers termination (with a timeout)
        for (rt in readerThreads) {
            rt.join(5000)
            if (rt.isAlive) {
                fail("too much execution time for reader thread")
            }
        }

        // final assertions

        Assertions.assertEquals(data_size, resultSet.size)
        Assertions.assertEquals(expectedSet, resultSet);
    }

         */
}