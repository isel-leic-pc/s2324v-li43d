package pt.isel.pc.first

import kotlin.concurrent.thread


fun main() {
    println("Hello from main thread ${Thread.currentThread().threadId()}")
    /*
    val child = Thread {
        println("Hello from child thread ${Thread.currentThread().threadId()}")
    }
    child.setDaemon(true)
    child.start()
     */

    val child = thread(start = false) {
        println("Hello from child thread ${Thread.currentThread().threadId()}")
    }
    child.start()
    child.join()
    println("after join!")
}