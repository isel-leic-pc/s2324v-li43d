package pt.isel.pc.monitors

import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * A synchronous queue support offer (add) and poll (take) operations
 * on a rendezvous form, that is, for each offering,
 * must exist a corresponding poller, and vice-versa
 * In this way, there is no need to maintain a list of offered elements
 * see docs for Java similar SynchronousQueue synchronizer
 */
class SynchronousQueue<E> {
    private val monitor = ReentrantLock()
    private val pollDone = monitor.newCondition()
    private val offerDone = monitor.newCondition()

    private class Waiter<E>(var elem: E? = null)

    private val pendingOffers = LinkedList<Waiter<E>>()

    private val pendingPolls = LinkedList<Waiter<E>>()

    private fun tryOffer(e: E) : Boolean {
        if (pendingPolls.isEmpty()) return false
        val pp = pendingPolls.removeFirst()
        pp.elem = e
        pollDone.signalAll()
        return true
    }

    private fun tryPoll() : E? {
        if (pendingOffers.isEmpty()) return null
        val po = pendingOffers.removeFirst()
        val e = po.elem
        po.elem = null
        offerDone.signalAll()
        return e
    }

    //@Throws(InterruptedException::class)
    fun offer(e: E) {
        offer(e, Duration.INFINITE)
    }

    @Throws(InterruptedException::class)
    fun offer(e: E, timeout: Duration) : Boolean {
        monitor.withLock {
            // fast path
            if (tryOffer(e)) return true

            if (timeout == Duration.ZERO) return false
            // wait path

            var timeoutNanos = timeout.inWholeNanoseconds
            val waiter = Waiter(e)
            pendingOffers.addLast(waiter)

            do {
                try {
                    if (timeout == Duration.INFINITE) {
                        offerDone.await()
                    }
                    else {
                        timeoutNanos = offerDone.awaitNanos(timeoutNanos)
                    }
                    if (waiter.elem == null)
                        return true
                    if (timeoutNanos <= 0) {
                        pendingOffers.remove(waiter)
                        return false
                    }
                }
                catch(e : InterruptedException) {
                    if (waiter.elem == null) {
                        Thread.currentThread().interrupt()
                        return true
                    }
                    pendingOffers.remove(waiter)
                    throw e
                }
            }
            while(true)
        }
    }

    @Throws(InterruptedException::class)
    fun poll() : E {
      monitor.withLock {
          // fast path
          val e = tryPoll()
          if (e != null) {
              return e!!
          }
          // wait path
          val waiter = Waiter<E>()
          pendingPolls.addLast(waiter)
          do {
              try {
                 pollDone.await()
                 if (waiter.elem != null)  {
                     return waiter.elem!!
                 }
              }
              catch(e: InterruptedException) {
                  if (waiter.elem != null) {
                      Thread.currentThread().interrupt()
                      return waiter.elem!!
                  }
                  pendingPolls.remove(waiter)
                  throw e
              }
          }
          while (true)

      }
    }

    @Throws(InterruptedException::class)
    fun poll(timeout: Duration) : E? {
        TODO()
    }
}