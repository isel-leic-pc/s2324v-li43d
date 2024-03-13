package pt.isel.pc.controlsynchro

import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * a FIFO queue with blocking put when full,
 * and blocking get when empty
 */
class Queue<T>(private val capacity : Int) {
    private val list = LinkedList<T>()
    private val mutex = ReentrantLock()
    private val canGet = Semaphore(0)
    private val spaceAvaiable = Semaphore(capacity)

    fun put(elem : T)  {
        spaceAvaiable.acquire()
        mutex.withLock {
            list.add(elem)
        }
        canGet.release()
    }

    fun get() : T {
        canGet.acquire()
        return mutex.withLock {
            list.removeFirst()
        }.also {
            spaceAvaiable.release()
        }
    }
}