package pt.isel.pc
/**
 * An implementation for a lock
 * with readers/writers semantic and readers priority
 * using semaphores
 * Unfortunately a version with writers priority is much more difficult
 * just with semaphores and mutexes we have not a pattern to construct
 * synchronizers and each synchronizer is a distinct challenge.
 *
 * As we will see, the monitor concept will permit building
 * readers/writers lock with different semantics with
 * the same difficulty level
 */
class ReadersWritersLock {
    fun startRead() {

    }

    fun endRead() {

    }

    fun startWrite() {

    }

    fun endWrite() {
        
    }
}