package pt.isel.pc

import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * An implementation for a lock
 * with readers/writers and readers priority behaviour,
 * using a semaphore and a reentrant lock.
 * Unfortunately a version with writers priority is much more difficult.
 * Just with semaphores and locks we don't have a pattern to construct
 * generic synchronizers, and each synchronizer is a distinct challenge.
 *
 * As we will see, the monitor concept will permit building
 * readers/writers lock with different semantics with
 * the same difficulty level
 */
class ReadersWritersLock {
    private var nreaders = 0
    private val readersLock = ReentrantLock()
    private val canAccess = Semaphore(1)

    /**
     * Entry for readers. If first reader is blocked
     * on canAccess.acquire (because there is an active writer),
     * the following readers will wait on readersLock.
     * Note that after first reader acquire the canAccess semaphore,
     * subsequent readers will immediately enter the region.
     * In this way, while there are readers, the potential writers can't proceed.
     */
    fun startRead() {
        readersLock.withLock {
            if (nreaders == 0) {
                canAccess.acquire()
            }
            nreaders++;
        }

    }

    /**
     * Exit for readers.
     * Last exited reader has the responsability to release
     * the access control semaphore.
     */
    fun endRead() {
        readersLock.withLock {
            if (--nreaders == 0) {
                // last reader
                canAccess.release()
            }
        }
    }

    /**
     * Entry for writers
     * The logic for writers is simple. Just use the
     * canAccess semaphore has an exclusive lock
     */
    fun startWrite() {
        canAccess.acquire()
    }

    /**
     * Exit for writers.
     * Just release the canAccess semaphore.
     */
    fun endWrite() {
        canAccess.release()
    }
}