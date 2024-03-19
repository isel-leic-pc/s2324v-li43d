package pt.isel.pc.monitors

import kotlin.time.Duration

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