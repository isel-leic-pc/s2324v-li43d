import pt.isel.pc.monitors.SynchronousQueue
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * SynchronousQueue version with n-ary poll.
 */
class SynchronousQueueMultiPoll<E> {
    private val monitor = ReentrantLock()
    private val offerDone = monitor.newCondition()
    private val pollDone = monitor.newCondition()

    private class PendingOffer<E>(var e: E?)

    private class PendingPoll<E>(val size: Int, var elems : List<E>? = null)

    private val pendingOffers = LinkedList<PendingOffer<E>>()

    private val pendingPolls = LinkedList<PendingPoll<E>>()

    /**
     * condition for fast path in offer.
     * There must be the samecnumber of offers as the requested
     * number of messages by the poll operation at the front of
     * pendingPolls
     */
    private fun tryOffer(e: E) : Boolean {
        if (pendingPolls.isEmpty() ||
            pendingPolls.first().size != pendingOffers.size +1)
            return false

        // now produce the list to deliver to pending poll at
        // the front of the queue
        val list = mutableListOf<E>()
        for(po in pendingOffers) {
            list.addLast(po.e)
            po.e = null
        }
        list.addLast(e)
        pendingOffers.clear()

        // now deliver the list to the poll operation at the front
        val pp = pendingPolls.removeFirst()
        pp.elems = list

        offerDone.signalAll()
        pollDone.signalAll()
        return true
    }

    fun offer(e: E) {
       offer(e, Duration.INFINITE)
    }

    @Throws(InterruptedException::class)
    fun offer(e: E, timeout: Duration): Boolean {
        // fast path
        if (tryOffer(e)) return true

        if (timeout == Duration.ZERO) return false

        var timeoutNanos = timeout.inWholeNanoseconds
        val waiter = PendingOffer(e)
        pendingOffers.addLast(waiter)

        do {
           try {
               if (timeout == Duration.INFINITE) {
                   offerDone.await()
               }
               else {
                   timeoutNanos = offerDone.awaitNanos(timeoutNanos)
               }
               if (waiter.e == null)
                   return true
               if (timeoutNanos <= 0) {
                   pendingOffers.remove(waiter)
                   return false
               }
           }
           catch(e : InterruptedException) {
               if (waiter.e == null) {
                   Thread.currentThread().interrupt()
                   return true
               }
               pendingOffers.remove(waiter)
               throw e
           }
        }
        while(true)

    }

    @Throws(InterruptedException::class)
    fun poll(nMsgs: Int): List<E> {
        TODO()
    }

    @Throws(InterruptedException::class)
    fun poll(nMsgs: Int, timeout: Duration): List<E>? {
        TODO()
    }
}