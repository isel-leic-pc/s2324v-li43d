package pt.isel.pc

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Counter(private var value: Long = 0) {
    private val mutex = ReentrantLock()

    fun inc() {
        /*
        mutex.lock()
        try {
            value++
        }
        finally {
            mutex.unlock()
        }
        */
        mutex.withLock {
            value++
        }

    }
    fun dec() {
        mutex.withLock {
            value--
        }
    }

    fun get() : Long {
        mutex.withLock {
            return value
        }
    }
}

class Counter2(private var value: Long = 0) {
    private val mutex = Any()

    fun inc() {
       synchronized(mutex) {
            value++
        }

    }
    fun dec() {
        synchronized(mutex) {
            value--
        }
    }

    fun get() : Long {
        synchronized(mutex) {
            return value
        }
    }
}