package pt.isel.pc

class Counter(private var value: Long = 0) {
    fun inc() {
        value++
    }
    fun dec() {
        value--
    }
    fun get() : Long {
        return value
    }
}