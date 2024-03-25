import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.math.min
import kotlin.random.Random
import kotlin.test.fail
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
    private val logger = KotlinLogging.logger {}


    /**
     * A more complicated test that should be done after the simple tests
     * have succeeded
     * In this case we have an arbitrary number of enqueuers and dequeuers
     * The test parametrizes the number of each.
     * Note that this code could be refactored to an auxiliary method that
     * could be used to build tests for different scenarios.
     */
    @Test
    fun blocking_message_queue_with_multiple_senders_receivers() {
        val NWRITERS = 4
        val NREADERS = 1
        val CAPACITY = 4

        val numbers_range = (1..1000)
        val numbers_size = numbers_range.count()


        val expectedSet = (1..numbers_size).toSet()

        val msgQueue = BlockingMessageQueue<Int>(CAPACITY)

        val resultSet = ConcurrentHashMap.newKeySet<Int>()

        // this index use is protected by a mutex
        // in oprder to support multiple writers
        var writeIndex = 1
        val mutex = ReentrantLock()

        val writerThreads = (1..NWRITERS)
            .map {
                thread {
                    logger?.info("start writer")
                    var random = Random(it)
                    while (true) {
                        var size = 0
                        var localIndex : Int
                        mutex.withLock {
                            localIndex = writeIndex
                            if (writeIndex <= numbers_size) {
                                size = min(numbers_size - writeIndex + 1,
                                    random.nextInt(1, 4))
                                writeIndex += size
                            }
                        }
                        if (size > 0) {
                            val value = (localIndex..<localIndex+size).toList()
                            logger?.info("writer try send $value")
                            if (msgQueue.tryEnqueue(value, Duration.INFINITE))
                                logger?.info("writer send $value")
                        } else {
                            break;
                        }

                    }
                    logger?.info("end writer")
                }
            }

        val readerThreads =  (1..NREADERS).map {
            thread {
                logger?.info("start reader")
                while(true) {
                    val value =
                        msgQueue.tryDequeue(3000.milliseconds) ?: break
                    resultSet.add(value)
                    logger?.info("reader get $value")
                }
                logger?.info("end reader")
            }
        }


        // wait for writers termination (with a timeout)
        for(wt in writerThreads) {
            wt.join(10000)
            if (wt.isAlive) {
                fail("too much execution time for writer thread")
            }
        }

        // wait for readers termination (with a timeout)
        for(rt in readerThreads) {
            rt.join(10000)
            if (rt.isAlive) {
                fail("too much execution time for reader thread")
            }
        }

        // final assertions


        Assertions.assertEquals(numbers_size, resultSet.size)
        Assertions.assertEquals(expectedSet, resultSet);
    }
}