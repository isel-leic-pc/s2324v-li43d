package pt.isel.pc.monitors

import kotlin.time.Duration

/**
 * A synchronous queue support offer (add) and poll (take) operations
 * on a rendezvous form, that is, for each offering,
 * must exist a corresponding poller, and vice-versa
 * In this way, there is no need to maintain a list of offered elements
 * see docs for Java similar SynchronousQueue synchronizer
 *
 */
class SynchronousQueue<E> {

    @Throws(InterruptedException::class)
    fun offer(e: E) {
        TODO()
    }

    @Throws(InterruptedException::class)
    fun offer(e: E, timeout: Duration) : Boolean {
        TODO()
    }

    @Throws(InterruptedException::class)
    fun poll() : E {
        TODO()
    }

    @Throws(InterruptedException::class)
    fun poll(timeout: Duration) : E? {
        TODO()
    }
}