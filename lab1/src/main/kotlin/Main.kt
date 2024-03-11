package pt.isel.pc

import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

fun launchSomeThreads(concurrencyLevel: Int, action : (Int)->Unit ) : List<Thread> {
    return (1 .. concurrencyLevel).map {id->
        thread {
            action(id)
        }
    }
}

fun main() {
    val NREDS = 5
    val NGREENS = 10

    val redThreads = launchSomeThreads(NREDS)  { id->
        repeat(10) { action->
            println("simulating some word done by red thread $id, action $action")
        }

    }
    val greenThreads = launchSomeThreads(NGREENS)  { id->
        repeat(20) { action ->
            println("simulating some word done by green thread $id, action $action")
        }
    }
    greenThreads.forEach {
        it.join()
    }
    redThreads.forEach {
        it.join()
    }
}