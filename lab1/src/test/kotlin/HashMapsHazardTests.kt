import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.system.measureTimeMillis

/**
 * multiple (some ok, some bad) variants of concurrent access to
 * an hashmap in order to update key values
 */
class HashMapsHazardTests {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val N_THREADS = Runtime.getRuntime().availableProcessors()
        private val N_KEYS = 5000000
    }

    /**
     * do in parallel some action
     */
    private fun parallelMapFiller(updater : (Int) -> Unit) : Long {
        return measureTimeMillis {
            (0..<N_THREADS)
                .map {
                    thread {
                        for (i in 1..N_KEYS) {
                            updater(i)
                        }
                    }
                }.forEach { thread ->
                    thread.join()
                }
        }
    }

    /**
     * return the sum of the hashMap int values
     */
    private fun hashSumValues(hashMap: MutableMap<Int, out Any>) : Int {
        return hashMap.values
            .map { value ->
                when(value) {
                    is Int -> value
                    is AtomicInteger -> value.get()
                    else -> 0
                }

            }
            .reduce{ a , e -> a+e }
    }

    @Test
    fun countWithBasicMapTest() {
        val basic = HashMap<Int, AtomicInteger>()

        val millis = parallelMapFiller { key ->
            val value = basic[key] ?: AtomicInteger()
            value.incrementAndGet()
            basic[key] = value
        }
        logger.error ("basic hashMap fill done in $millis ms!")
        Assertions.assertEquals(N_THREADS * N_KEYS, hashSumValues(basic))
    }

    @Test
    fun countWithSynchronizedMapTest() {
        val synchMap: MutableMap<Int, AtomicInteger> =
            Collections.synchronizedMap(HashMap())

        val millis = parallelMapFiller { key->
            val value = synchMap[key] ?: AtomicInteger()
            value.incrementAndGet()
            synchMap[key] = value
        }
        logger.error("synchronized hashMap fill done in $millis ms!")
        Assertions.assertEquals(N_THREADS * N_KEYS, hashSumValues(synchMap))
    }

    @Test
    fun countWithOurSynchroMapTest() {
        val ourSynchroMap = HashMap<Int, Int>()
        val mutex = ReentrantLock()

        val millis = parallelMapFiller { key ->
            mutex.withLock {
                val value = ourSynchroMap[key] ?: 0
                ourSynchroMap[key] = value + 1
            }
        }

        logger.error("our synchro hashMap fill done in $millis ms!")
        Assertions.assertEquals(N_THREADS * N_KEYS, hashSumValues(ourSynchroMap))
    }

    @Test
    fun countWithConcurrentMapTest() {
        val concurrentMap: MutableMap<Int, AtomicInteger> = ConcurrentHashMap()

        val millis = parallelMapFiller { key ->
            concurrentMap
                .computeIfAbsent(key) { value -> AtomicInteger() }
                .incrementAndGet()
        }
        logger.error("concurrent hashMap fill done in $millis ms!")
        Assertions.assertEquals(N_THREADS * N_KEYS, hashSumValues(concurrentMap))
    }

}