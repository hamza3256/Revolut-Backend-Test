package customers.accounts.transactions

import Currencies.USD
import Customers
import USD
import customers.accounts.Account
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TransactionCreatorImplTest {

    private lateinit var transactionCreator: TransactionCreator
    private lateinit var transactionRepository: TransactionRepository

    @Before
    fun beforeEachTest() {
        transactionRepository = InMemoryTransactionRepository()
        transactionCreator = TransactionCreatorImpl(transactionRepository)
    }

    @Test
    fun `should insert 2 transactions into the repository`() {
        val nikolay = Customers.nikolay(0)
        val nikolaysAccount = Account(0, nikolay, USD)
        val vlad = Customers.vlad(1)
        val vladsAccount = Account(1, vlad, USD)

        val request = TransactionCreator.Request(
            money = 0.USD,
            from = nikolaysAccount,
            to = vladsAccount
        )

        transactionRepository.run {
            assertTrue(getAll(nikolaysAccount).isEmpty())
            assertTrue(getAll(vladsAccount).isEmpty())

            transactionCreator.createTransferTransactions(request)

            assertEquals(1, getAll(nikolaysAccount).size)
            assertEquals(1, getAll(vladsAccount).size)
        }
    }

    @Test
    fun `should be able to resolve both transactions from one another`(){
        val nikolay = Customers.nikolay(0)
        val nikolaysAccount = Account(0, nikolay, USD)
        val vlad = Customers.vlad(1)
        val vladsAccount = Account(1, vlad, USD)

        val request = TransactionCreator.Request(
            money = 0.USD,
            from = nikolaysAccount,
            to = vladsAccount
        )

        transactionCreator.createTransferTransactions(request)

        transactionRepository.run {
            val nikolaysTransaction = getAll(nikolaysAccount).first()
            val vladsTransaction = getAll(vladsAccount).first()

            assertEquals(vladsTransaction.id, nikolaysTransaction.mirrorTransactionId)
            assertEquals(nikolaysTransaction.id, vladsTransaction.mirrorTransactionId)
        }
    }

    @Test
    fun `returned transactions should be correct`(){
        val nikolay = Customers.nikolay(0)
        val nikolaysAccount = Account(0, nikolay, USD)
        val vlad = Customers.vlad(1)
        val vladsAccount = Account(1, vlad, USD)

        val request = TransactionCreator.Request(
            money = 0.USD,
            from = nikolaysAccount,
            to = vladsAccount
        )

        val createdTransactions = transactionCreator.createTransferTransactions(request)

        transactionRepository.run {
            val nikolaysTransaction = getAll(nikolaysAccount).first()
            val vladsTransaction = getAll(vladsAccount).first()

            assertEquals(createdTransactions.fromTransaction, nikolaysTransaction)
            assertEquals(createdTransactions.toTransaction, vladsTransaction)
        }
    }
}