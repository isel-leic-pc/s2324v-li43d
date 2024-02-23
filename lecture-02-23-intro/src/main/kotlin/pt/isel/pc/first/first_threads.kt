package pt.isel.pc.first

import kotlin.concurrent.thread


fun main() {
    println("Hello from main thread ${Thread.currentThread().threadId()}")
}