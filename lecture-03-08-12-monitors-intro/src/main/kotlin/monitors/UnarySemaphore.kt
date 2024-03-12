package pt.isel.pc.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class UnarySemaphore(private var permits : Int = 0) {
    val monitor = ReentrantLock()
    val availablePermits = monitor.newCondition()

    fun acquire() {

        monitor.withLock {
            // fast path
            if (permits > 0) {
                --permits
                return
            }
            // wait path
            do {
                availablePermits.await()
                if (permits > 0) {
                    --permits
                    return
                }
            }
            while(true)
        }
    }

    fun release() {
        monitor.withLock {
            ++permits
            availablePermits.signal()
        }
    }
}