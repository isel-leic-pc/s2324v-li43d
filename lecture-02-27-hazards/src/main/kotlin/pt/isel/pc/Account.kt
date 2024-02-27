package pt.isel.pc

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Account(private var balance : Long = 0) {
    companion object {
        private val sharedMutex = ReentrantLock()
    }

    private val mutex = ReentrantLock()

    fun transfer0(other: Account, toTransfer : Long) : Boolean {
        if (balance < toTransfer) return false
        balance -= toTransfer

        other.balance += toTransfer
        return true
    }


    fun transfer1(other: Account, toTransfer : Long) : Boolean {
        sharedMutex.withLock {
            if (balance < toTransfer) return false
            balance -= toTransfer

            other.balance += toTransfer
            return true
        }
    }

    fun transfer2(other: Account, toTransfer : Long) : Boolean {
        mutex.withLock {
            if (balance < toTransfer) return false

            other.mutex.withLock {
                balance -= toTransfer
                other.balance += toTransfer
            }
            return true
        }
    }

    fun transfer3(other: Account, toTransfer : Long) : Boolean {
        val (mtx1, mtx2) = if (mutex.hashCode() < other.mutex.hashCode())
            Pair(mutex, other.mutex)
        else
            Pair(other.mutex, mutex)

        mtx1.withLock  {
            mtx2.withLock {
                if (balance < toTransfer) return false
                balance -= toTransfer
                other.balance += toTransfer
            }
            return true
        }
    }

    fun getBalance() : Long {
        mutex.withLock {
            return balance
        }
    }
}