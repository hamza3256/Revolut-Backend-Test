package clients.accounts

import Clients
import Currencies.GBP
import Currencies.USD
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InMemoryAccountRepositoryTest {

    private lateinit var repository: AccountRepository
    private val client = Clients.nikolay()

    @Before
    fun beforeEachTest() {
        repository = InMemoryAccountRepository()
    }

    @Test
    fun `adding an account for the first time should succeed`() {
        val account = Account(0, client, USD)

        assertTrue(repository.addAccount(account))
        assertEquals(account, repository.getAccount(client, USD))
    }

    @Test
    fun `adding an account when the client already has an account with the given currency should return false`() {
        val usdAccount = Account(0, client, USD)
        val usdAccountCopy = usdAccount.copy(id = 1)

        assertTrue(repository.addAccount(usdAccount))
        assertFalse(repository.addAccount(usdAccountCopy))
    }

    @Test
    fun `adding multiple accounts with different currencies should succeed`() {
        val usdAccount = Account(0, client, USD)
        val gbpAccount = Account(1, client, GBP)

        assertTrue(repository.addAccount(usdAccount))
        assertTrue(repository.addAccount(gbpAccount))
        assertEquals(setOf(usdAccount, gbpAccount), repository.getAccounts(client))
    }

    @Test
    fun `getAccount() should return null for client without an account for the given currency`() {
        val usdAccount = Account(0, client, USD)
        repository.addAccount(usdAccount)

        assertNull(repository.getAccount(client, GBP))
    }

    @Test
    fun `getAccounts() should be empty when no accounts created`() {
        assertTrue(repository.getAccounts(client).isEmpty())
    }
}