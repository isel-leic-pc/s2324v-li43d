
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import pt.isel.pc.NodeList

class NodeListTests {
    @Test
    fun `NodeList simple creation Test`() {
        val list = NodeList<Int>()
        Assertions.assertTrue(list.isEmpty)

        list.addLast(3)
        Assertions.assertFalse(list.isEmpty)
        Assertions.assertEquals(1, list.size)
        Assertions.assertEquals(3, list.first)

        val res = list.removeFirst()
        Assertions.assertTrue(list.isEmpty)
        Assertions.assertTrue(res is NodeList.Node<Int>)
        Assertions.assertEquals(3, res.value)

        list.addLast(5)
        val n = list.addLast(6)
        list.addLast(7)
        Assertions.assertEquals(3, list.size)
        list.remove(n)
        Assertions.assertEquals(2, list.size)
        Assertions.assertEquals(5, list.first)
        Assertions.assertEquals(7, list.last)

        val list1 : List<Int> = mutableListOf()
        for (i in list) {
            list1.addLast(i)
        }
        Assertions.assertIterableEquals(list1, list)
        list1.forEach {
            println(it)
        }
        val n2 = list.removeFirst()
        Assertions.assertEquals(7, list.last)
        Assertions.assertEquals(7, list.first)
        Assertions.assertEquals(1, list.size)
        val n3 = list.addFirst(8)
        Assertions.assertTrue(res is NodeList.Node<Int>)
        Assertions.assertEquals(8, n3.value)
        list.clear()
        Assertions.assertEquals(0, list.size)
        val n4 = list.addFirst(10)
        Assertions.assertTrue(res is NodeList.Node<Int>)
        Assertions.assertEquals(10, n4.value)
    }
}