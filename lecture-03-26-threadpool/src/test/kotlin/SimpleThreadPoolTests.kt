import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SimpleThreadPoolTests {
    val TST_TIMEOUT = 2000L // milliseconds
    @Test
    fun `simple thread pool with classic idiom execute one cmd test`() {
        val pool = SimpleThreadPoolExecutor0(2)
        val cdl = CountDownLatch(1)
        var result = 0

        pool.execute {
            result = 2
            cdl.countDown()
        }

        cdl.await(TST_TIMEOUT, TimeUnit.MILLISECONDS)
        Assertions.assertEquals(2, result)
    }

    @Test
    fun `simple thread pool with kernel style execute one cmd test`() {
        val pool = SimpleThreadPoolExecutor(2)
        val cdl = CountDownLatch(1)
        var result = 0

        pool.execute {
            result = 2
            cdl.countDown()
        }

        cdl.await(TST_TIMEOUT, TimeUnit.MILLISECONDS)
        Assertions.assertEquals(2, result)
    }
}