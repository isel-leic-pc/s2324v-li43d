import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

/**
 * A simple ThreadPoolExecutor without shutdown and
 * destruction of inactive threads, using monitor classic idiom
 * for threads waiting for work
 */
class SimpleThreadPoolExecutor0(private val maxThreadPoolSize: Int)  {

    private val workUnits = LinkedList<Runnable>()
    private val monitor =ReentrantLock()
    private val workAvaialable = monitor.newCondition()

    private var poolThreads = 0
    private var availableThreads = 0

    private fun safeRun(cmd: Runnable) {
        try {
            cmd?.run()
        }
        catch(e: Exception) {
            // swallow exception is not bad here!
            // just avoid premature worker thread termination
            // due to a bad job
        }
    }

    /**
     * private monitor operation used by worker threads
     *
     */
    private fun runLoop(startCmd: Runnable) {
        var cmd = startCmd
        while(true) {
            // most important: run the job cmd out of the
            // lock in other to enable parallelism
            // without this, the thread poll is a nonsense
            safeRun(cmd)
            monitor.withLock {
                // fast path
                if (workUnits.isNotEmpty()) {
                    cmd = workUnits.removeFirst()
                }
                else {
                    // wait path
                    availableThreads++

                    do {
                        workAvaialable.await()
                        // on classic idiom, our choice
                        // was defined by the condition scheduler that
                        // we don't know. If we want FIFO or LIFO scheduling,
                        // we should user "kernel style"
                        // Here, we just recheck the work queue for more job
                        if (workUnits.isNotEmpty())  {
                           availableThreads--
                           cmd = workUnits.removeFirst()
                           break
                       }
                    }
                    while(true)
                }
            }
        }
    }

    fun execute(cmd : Runnable) {
        monitor.withLock {
            if (availableThreads > 0) {
                // on classical idiom. we
                // put the cmd to the work queue...
                workUnits.addLast(cmd)
                //... and just signal an arbitrary thread to recheck the queue
                workAvaialable.signal()
            }
            else if (poolThreads < maxThreadPoolSize) {
                // create and start a new thread and pass the job to that one
                Thread {
                    runLoop(cmd)
                }.start()
            }
            else {
                // if all worker threads are active, just put the job on thw job queue
                workUnits.addLast(cmd)
            }
        }
    }
}

/**
 * A simple ThreadPoolExecutor without shutdown and
 * destruction of inactive threads, using "kernel style" idiom
 * in order to proper schedulling of inactive worker threads
 * (in this case FIFO, but LIFO could be more appropriate).
 */
class SimpleThreadPoolExecutor(private val maxThreadPoolSize: Int)  {

    private val workUnits = LinkedList<Runnable>()
    private val monitor =ReentrantLock()

    private var poolThreads = 0
    private var availableThreads = 0

    // an object to represent an inactive thread
    // each object has a distinct condition, to enable specific notification.
    private class WorkerThread(val workDelivered : Condition, var cmd : Runnable? = null)


    // the list of inactive worker threads
    private val pendingWorkers = LinkedList<WorkerThread>()

    private fun safeRun(cmd: Runnable) {
        try {
            cmd?.run()
        }
        catch(e: Exception) {
            // swallow exception is not bad here!
            // just avoid premature worker thread termination
            // due to a bad job
        }
    }

    /**
     * private monitor operation used by worker threads
     *
     */
    private fun runLoop(startCmd: Runnable) {
        var cmd : Runnable = startCmd
        while(true) {
            // most important: run the job cmd out of the
            // lock in other to enable parallelism
            // without this, the thread poll is a nonsense
            safeRun(cmd)
            monitor.withLock {
                // fast path
                if (workUnits.isNotEmpty()) {
                    cmd = workUnits.removeFirst()
                }
                else {
                    // wait path
                    // according to "kernel style" we must have an object
                    // to represent our request for a job
                    // Note that it is a waste to create a new one for each request,
                    // it was better to create an object in the beginning of the run loop
                    // and reuse it as needed
                    val waiter = WorkerThread(monitor.newCondition())
                    pendingWorkers.addLast(waiter)
                    do {
                        waiter.workDelivered.await()
                        // in kernel style we just evaluate our local state, that out«r WorkerThread object
                        if (waiter.cmd != null)  {
                            cmd = waiter.cmd!!
                            break
                        }
                    }
                    while(true)
                }
            }
        }
    }

    fun execute(cmd : Runnable) {
        monitor.withLock {
            if (pendingWorkers.isNotEmpty()) {
                // in kernel style we resolve the work request know
                // we remove it from the job queue and pass the Runnable
                // to the thread associated to removed work request
                // since is used specific notification, we guarantee
                // the we are awaking the right thread
                val pw = pendingWorkers.removeFirst()
                pw.cmd = cmd
                pw.workDelivered.signal()
            }
            else if (poolThreads < maxThreadPoolSize) {
                // create and start a new thread and pass the job to that one
                Thread {
                    runLoop(cmd)
                }.start()
            }
            else {
                // if all worker threads are active, just put the job on thw job queue
                workUnits.addLast(cmd)
            }
        }
    }
}