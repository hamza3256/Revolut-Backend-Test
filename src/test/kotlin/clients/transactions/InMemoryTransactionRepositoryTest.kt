package clients.transactions

import Clients
import Currencies.GBP
import Currencies.USD
import GBP
import USD
import clients.accounts.Account
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InMemoryTransactionRepositoryTest {

    private lateinit var repository: TransactionRepository

    @Before
    fun beforeEachTest() {
        repository = InMemoryTransactionRepository()
    }

    @Test
    fun `adding unique transactions should succeed`() {
        val client = Clients.vlad()
        val account = Account(0, client, USD)

        val transaction = Transaction(id = 0, account = account, money = 10.USD)
        assertTrue(repository.add(transaction))
    }

    @Test
    fun `adding a second transaction with the same id should fail`() {
        val client = Clients.vlad()
        val account = Account(0, client, USD)

        val transaction = Transaction(id = 0, account = account, money = 10.USD)
        assertTrue(repository.add(transaction))

        val copy = transaction.copy()
        assertFalse(repository.add(copy))
    }

    @Test
    fun `getAll(client) should return empty list when no transactions have been committed`() {
        val client = Clients.nikolay()

        assertTrue(repository.getAll(client).isEmpty())
    }

    @Test
    fun `getAll(client) should return transactions which were committed`() {
        val client = Clients.nikolay()
        val account = Account(0, client, USD)

        //prepare transactions to add to repository
        val expected = (1..3).map { index ->
            Transaction(id = index.toLong(), account = account, money = index.USD)
        }.toSet()

        //make sure the repository doesn't contain those transactions yet
        assertNotEquals(expected, repository.getAll(client))

        //add transactions and verify added correctly
        expected.forEach { transaction ->
            assertTrue(repository.add(transaction))
        }

        //should return the same set now
        assertEquals(expected, repository.getAll(client))
    }

    @Test
    fun `getAll(client) should return only transactions from respective client`() {
        //add a USD transaction for vlad
        val vlad = Clients.vlad(0)
        val vladsUsdAccounts = Account(0, vlad, USD)
        val vladsUsdTransaction = Transaction(0, account = vladsUsdAccounts, money = 10.USD)
        assertTrue(repository.add(vladsUsdTransaction))

        //add a USD transaction for nikolay
        val nikolay = Clients.nikolay(1)
        val nikolaysUsdAccount = Account(1, nikolay, USD)
        val nikolaysUsdTransaction = Transaction(1, account = nikolaysUsdAccount, money = 20.USD)
        assertTrue(repository.add(nikolaysUsdTransaction))

        //getAll() should return respective transactions for each client
        assertEquals(setOf(vladsUsdTransaction), repository.getAll(vlad))
        assertEquals(setOf(nikolaysUsdTransaction), repository.getAll(nikolay))
    }

    @Test
    fun `getAll(account) should return empty list when no transactions have been committed`() {
        val client = Clients.nikolay()
        val account = Account(0, client, USD)

        assertTrue(repository.getAll(account).isEmpty())
    }

    @Test
    fun `getAll(account) should only return transactions only from the given accounts currency`() {
        val client = Clients.vlad()

        //add a transaction for USD
        val usdAccount = Account(0, client, USD)
        val usdTransaction = Transaction(0, account = usdAccount, money = 0.USD)
        repository.add(usdTransaction)

        //add a transaction for GBP
        val gbpAccount = Account(0, client, GBP)
        val gbpTransaction = Transaction(1, account = gbpAccount, money = 0.GBP)
        repository.add(gbpTransaction)

        //make sure return transactions match for each account
        assertEquals(setOf(gbpTransaction), repository.getAll(gbpAccount))
        assertEquals(setOf(usdTransaction), repository.getAll(usdAccount))
    }

    @Test
    fun `getAll(account) shouldn't return transactions relating to other clients accounts of the same currency`() {
        val nikolay = Clients.nikolay(0)
        val vlad = Clients.vlad(1)

        //insert USD transactions for each clients USD account
        //nikolay
        val nikolaysUsdAccount = Account(0, nikolay, USD)
        val nikolaysUsdTransaction = Transaction(0, account = nikolaysUsdAccount, money = 10.USD).also { transaction ->
            assertTrue(repository.add(transaction))
        }
        //vlad
        val vladsUsdAccount = Account(1, vlad, USD)
        val vladsUsdTransaction = Transaction(1, account = vladsUsdAccount, money = 20.USD).also { transaction ->
            assertTrue(repository.add(transaction))
        }

        //should return respective transactions
        assertEquals(
            setOf(nikolaysUsdTransaction),
            repository.getAll(nikolaysUsdAccount)
        )
        assertEquals(
            setOf(vladsUsdTransaction),
            repository.getAll(vladsUsdAccount)
        )
    }

    @Test
    fun `deleteAll() implies getAll() is empty`() {
        val vlad = Clients.vlad()
        repository.add(Transaction(0, account = Account(0, vlad, USD), money = 100.USD))
        assertTrue(repository.getAll(vlad).isNotEmpty())
        repository.deleteAll()
        assertTrue(repository.getAll(vlad).isEmpty())
    }
}