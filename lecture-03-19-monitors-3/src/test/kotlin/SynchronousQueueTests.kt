import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import pt.isel.pc.monitors.SynchronousQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.math.min
import kotlin.random.Random
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
    fun `simple rendezvous deliver with SinchronousQueueMultiPoll`() {
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
     * A more complicated test that should be done after the simple tests have succeeded
     * In this case we have an arbitrary number of enqueuers and dequeuers
     * The test parametrizes the number of each.
     * Note that this code could be refactored to an auxiliary method that
     * could be used to build tests for different scenarios.
     */
    @Test
    fun blocking_synchronous_queue_multi_poll_with_multiple_senders_receivers() {
        val NWRITERS = 4
        val NREADERS = 3

        val numbers_range = (1..1000)
        val input_size = numbers_range.count()

        val msgQueue = SynchronousQueueMultiPoll<Int>()
        val resultArray =  Array<List<Int>?>(input_size) { null }

        // this index use is protected by a mutex
        // in oprder to support multiple writers
        var writeIndex = 1

        // this index use is protected by a mutex
        // in order to support multiple writers
        var readIndex = 1

        var readCount = AtomicInteger()
        val writersMutex = ReentrantLock()
        val readersMutex = ReentrantLock()
        val writerThreads = (1..NWRITERS)
            .map {
                thread {
                    logger?.info("start writer")
                    while (true) {
                        var localIdx = 0
                        writersMutex.withLock {
                            if (writeIndex <= input_size) {
                                localIdx = writeIndex++
                            }
                        }
                        if (localIdx > 0 && localIdx <= input_size) {
                            logger?.info("writer try send $localIdx")
                            if (msgQueue.offer(localIdx, Duration.INFINITE))
                                logger?.info("writer send $localIdx")
                        } else {
                            break;
                        }

                    }
                    logger?.info("end writer")
                }
            }

        val readerThreads =  (0..< NREADERS).map {
            thread {
                logger?.info("start reader")
                val random = Random(it)
                while(true) {
                    var size = 0

                    readersMutex.withLock {

                        if (readIndex <= input_size) {
                            size = min(input_size -readIndex + 1,
                                random.nextInt(1, 5))
                            readIndex+= size
                        }
                    }
                    if (size == 0) break
                    val list =
                        msgQueue.poll(size, 100.milliseconds)
                    resultArray[readCount.getAndIncrement()] = list
                    logger?.info("reader get value $list")
                }

                logger?.info("end reader")
            }
        }


        // wait for writers termination (with a timeout)
        for(wt in writerThreads) {
            wt.join(5000)
            if (wt.isAlive) {
                fail("too much execution time for writer thread")
            }
        }

        // wait for readers termination (with a timeout)
        for(rt in readerThreads) {
            rt.join(5000)
            if (rt.isAlive) {
                fail("too much execution time for reader thread")
            }
        }

        // final assertions
        val expectedList = numbers_range.toList()

        val result = resultArray.toList().flatMap {
            it ?: listOf()
        }.sorted()


        Assertions.assertEquals(expectedList.size, result.size)
        Assertions.assertEquals(expectedList, result);
    }
}