package pt.isel.pc

/**
 * A double linked list implementation that export the list nodes, in order
 * to support efficient O(1) remove operations in all scenarios
 * (head, tail an intermediate nodes remove).
 * This is important since almost always lists are used on
 * kernel style (delegation of execution) monitors
 */
class NodeList<T> : Iterable<T> {

    private inner class ListIterator :Iterator<T> {
        var current = head.next

        override fun hasNext(): Boolean {
            return  (current  != head)
        }

        override fun next(): T {
            if (!hasNext()) throw IllegalStateException()
            val currVal = current.value!!
            current = current.next
            return currVal
        }
    }

    class Node<T>() {
        private var _val : T? = null
        internal var next : Node<T> = this
        internal var previous : Node<T> = this

        companion object {
            fun <T> insertBefore(_value : T, _node : Node<T> ) : Node<T> {
                return Node<T>(_value, _node, before = true)
            }

            fun <T> insertAfter(_value : T, _node : Node<T> ) : Node<T> {
                return Node<T>(_value, _node, before = false)
            }
        }

        var value : T get()  = _val!!
            set(v: T) { _val = v}

        private fun _insertBefore( _node : Node<T> ) {
            previous = _node.previous
            previous.next = this

            _node.previous = this
            next = _node;
        }

        private fun _insertAfter( _node : Node<T> ) {
            next = _node.next
            next.previous = this
            _node.next = this
            previous = _node;
        }

        private constructor(_value : T, _node : Node<T>, before: Boolean) : this() {
            _val = _value
            if (before) _insertBefore(_node)
            else  _insertAfter(_node)
        }

        internal fun remove() {
            previous.next = next
            next.previous = previous
        }
    }

    private var head = Node<T>()
    private var count = 0

    val size : Int get() = count
    val isEmpty : Boolean get() = count == 0

    fun addLast(value : T)  : Node<T> {
        count++
        return Node.insertBefore(value, head)
    }

    fun addFirst(value : T) : Node<T> {
        count++
        return Node.insertAfter(value, head)
    }

    fun removeFirst() : Node<T> {
        if (isEmpty)
            throw IllegalStateException()
        val node =  head.next
        node.remove()
        count--
        return node
    }


    fun remove(node : Node<T> ) {
        if (isEmpty)
            throw IllegalStateException()
        node.remove()
        count--
    }

    val first : T
        get() {
            if (isEmpty)
                throw IllegalStateException()
            return head.next.value!!
        }

    val last : T
        get() {
            if (isEmpty)
                throw IllegalStateException()
            return head.previous.value!!
        }

    override fun iterator(): Iterator<T> {
        return ListIterator()
    }

    // create a new empty list
    fun clear() {
        head = Node<T>()
        count = 0
    }

    internal fun show() {
        forEach {
            println(it)
        }
        println()
    }
}