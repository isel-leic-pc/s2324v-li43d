package pt.isel.pc

import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import kotlin.concurrent.thread

private val logger = KotlinLogging.logger {}

class ThreadTests {

    @Test
    fun `sleep interruption test`() {

        val t = thread {
            try {
                repeat(500) {
                    logger.info("is interrupted? ${Thread.currentThread().isInterrupted}")
                }
                logger.info("now, I sleep for 5 seconds")
                sleep(5000)
            }
            catch(e: InterruptedException) {
                logger.info("I have been interrupted!")
            }

        }
        sleep(20)
        t.interrupt()
        logger.info("test: interruption done!")
        t.join(3000)
        logger.info("test: terminated!")
    }
}