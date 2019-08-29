package money.account

import Currencies.GBP
import Currencies.USD
import clients.Client
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InMemoryAccountRepositoryTest {

    lateinit var repository: AccountRepository
    private val client = Client(0, "Nikolay", "Storonsky")

    @Before
    fun beforeEachTest() {
        repository = InMemoryAccountRepository()
    }

    @Test
    fun `adding an account for the first time should succeed`() {
        val account = Account(0, USD, emptyList(), 0.toBigDecimal())

        assertTrue(repository.addAccount(client, account))
        assertEquals(account, repository.getAccount(client, USD))
    }

    @Test
    fun `adding an account when the client already has an account with the given currency should return false`() {
        val usdAccount = Account(0, USD, emptyList(), 0.toBigDecimal())
        val usdAccountCopy = usdAccount.copy(id = 1)

        assertTrue(repository.addAccount(client, usdAccount))
        assertFalse(repository.addAccount(client, usdAccountCopy))
    }

    @Test
    fun `adding multiple accounts with different currencies should succeed`() {
        val usdAccount = Account(0, USD, emptyList(), 0.toBigDecimal())
        val gbpAccount = Account(1, GBP, emptyList(), 0.toBigDecimal())

        assertTrue(repository.addAccount(client, usdAccount))
        assertTrue(repository.addAccount(client, gbpAccount))
        assertEquals(setOf(usdAccount, gbpAccount), repository.getAccounts(client))
    }

    @Test
    fun `getAccount() should return null for client without an account for the given currency`() {
        val usdAccount = Account(0, USD, emptyList(), 0.toBigDecimal())
        repository.addAccount(client, usdAccount)

        assertNull(repository.getAccount(client, GBP))
    }

    @Test
    fun `getAccounts() should be empty when no accounts created`() {
        assertTrue(repository.getAccounts(client).isEmpty())
    }
}