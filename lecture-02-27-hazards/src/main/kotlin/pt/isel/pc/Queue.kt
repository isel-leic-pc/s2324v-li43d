package pt.isel.pc

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Queue<T> {
    private val list = mutableListOf<T>()
    private val mutex = ReentrantLock()

    fun put(elem : T)  {
        mutex.withLock {
            list.add(elem)
        }
    }


    fun get() : T? {
        mutex.withLock {
            if (list.isEmpty()) return null
            return list.removeFirst()
        }
    }

}