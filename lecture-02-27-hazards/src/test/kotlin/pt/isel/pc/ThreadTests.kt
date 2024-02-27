package pt.isel.pc

import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

class ThreadTests {

    @Test
    fun `many platform threads launched`() {
        val threads = mutableListOf<Thread>()
        repeat(1_000_000) {
            val t = Thread.ofPlatform().start() {
                var count= 0
                //println("hello from thread $it")
                repeat(100) {
                    //Thread.sleep(10)
                    count++
                }
                //println("thread $it gone with count = $count")
            }
            threads.add(t)
        }

        threads.forEach {
            it.join()
        }
    }

    @Test
    fun `many virtual threads launched`() {
        val threads = mutableListOf<Thread>()
        repeat(1_000_000) {
            val t = Thread.ofVirtual().start {
                var count= 0
                //println("hello from thread $it")
                repeat(100) {
                    //Thread.sleep(10)
                    count++
                }
                //println("thread $it gone with count = $count")
            }
            threads.add(t)
        }

        threads.forEach {
            it.join()
        }
    }
}