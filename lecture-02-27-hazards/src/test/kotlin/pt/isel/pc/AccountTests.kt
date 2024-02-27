package pt.isel.pc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

class AccountTests {
    private val INITIAL_BALANCE = 20000L
    private val TRANSFER_VALUE = 1000L

    private fun multipleTransfersBetween2Accounts(acc1: Account, acc2: Account,
                                                  transferOp : (src:Account, dst:Account) -> Unit) {
        val NITERS = 100000

        val thread1 = thread {
            repeat(NITERS) {
                transferOp(acc1, acc2)
                transferOp(acc2, acc1)
            }
        }

        val thread2 = thread {
            repeat(NITERS) {
                transferOp(acc2, acc1)
                transferOp(acc1, acc2)
            }
        }

        thread1.join()
        thread2.join()
    }

    @Test
    fun `multiple thread account access without any synchronization`() {

        val acc1 = Account(INITIAL_BALANCE)
        val acc2 = Account(INITIAL_BALANCE)

        multipleTransfersBetween2Accounts(acc1, acc2) {
              src, dst -> src.transfer0(dst, TRANSFER_VALUE)
        }

        assertEquals(INITIAL_BALANCE, acc1.getBalance())
        assertEquals(INITIAL_BALANCE, acc2.getBalance())
    }

    @Test
    fun `multiple thread account access using shared lock synchronization`() {

        val acc1 = Account(INITIAL_BALANCE)
        val acc2 = Account(INITIAL_BALANCE)

        multipleTransfersBetween2Accounts(acc1, acc2) {
                src, dst -> src.transfer1(dst, TRANSFER_VALUE)
        }

        assertEquals(INITIAL_BALANCE, acc1.getBalance())
        assertEquals(INITIAL_BALANCE, acc2.getBalance())
    }

    @Test
    fun `multiple thread account access using account locks synchronization`() {

        val acc1 = Account(INITIAL_BALANCE)
        val acc2 = Account(INITIAL_BALANCE)

        multipleTransfersBetween2Accounts(acc1, acc2) {
                src, dst -> src.transfer2(dst, TRANSFER_VALUE)
        }
        assertEquals(INITIAL_BALANCE, acc1.getBalance())
        assertEquals(INITIAL_BALANCE, acc2.getBalance())
    }

    @Test
    fun `multiple thread account access using account locks ordered by hashcode`() {

        val acc1 = Account(INITIAL_BALANCE)
        val acc2 = Account(INITIAL_BALANCE)

        multipleTransfersBetween2Accounts(acc1, acc2) {
                src, dst -> src.transfer3(dst, TRANSFER_VALUE)
        }
        assertEquals(INITIAL_BALANCE, acc1.getBalance())
        assertEquals(INITIAL_BALANCE, acc2.getBalance())
    }
}