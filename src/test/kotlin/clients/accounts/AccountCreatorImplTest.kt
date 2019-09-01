package clients.accounts

import Clients
import Currencies.GBP
import Currencies.USD
import GBP
import USD
import clients.accounts.AccountCreator.Request
import clients.accounts.AccountCreator.Result.Created
import clients.accounts.AccountCreator.Result.NegativeMoney
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AccountCreatorImplTest {

    private lateinit var accountRepository: AccountRepository
    private lateinit var accountCreator: AccountCreatorImpl

    private val client = Clients.nikolay()

    @Before
    fun beforeEachTest() {
        accountRepository = InMemoryAccountRepository()
        accountCreator = AccountCreatorImpl(accountRepository)
    }

    @Test
    fun `creating account for client with no accounts should succeed`() {
        val usdAccountRequest = Request(0.USD)
        val result = accountCreator.create(client, usdAccountRequest)

        assert(result is Created)
    }

    @Test
    fun `created account should have correct currency`() {
        val usdAccountRequest = Request(0.USD)
        val gbpAccountRequest = Request(0.GBP)

        val usdResult = accountCreator.create(client, usdAccountRequest) as Created
        val gbpResult = accountCreator.create(client, gbpAccountRequest) as Created

        assertEquals(USD, usdResult.account.currency)
        assertEquals(GBP, gbpResult.account.currency)
    }

    @Test
    fun `creating account with starting money should return Account with correct starting money`() {
        val usdAccountRequest = Request(1000.USD)
        val result = accountCreator.create(client, usdAccountRequest) as Created

        assertEquals(1000.USD, result.account.startingMoney)
    }

    @Test
    fun `creating account with zero money should succeed`() {
        val request = Request(0.USD)
        val result = accountCreator.create(client, request)

        assertTrue(result is Created)
    }

    @Test
    fun `creating account with negative money should fail`() {
        val request = Request((-1).USD)
        val result = accountCreator.create(client, request)

        assertTrue(result is NegativeMoney)
    }

    @Test
    fun `creating account with same currency should succeed`() {
        val usdAccountRequest = Request(1.USD)
        accountCreator.create(client, usdAccountRequest)

        val secondUsdAccountRequest = Request(2.USD)
        val result = accountCreator.create(client, secondUsdAccountRequest)

        assertTrue(result is Created)
    }

    @Test
    fun `creating account with same currency shouldn't overwrite previous account`() {
        val usdAccountRequest = Request(1.USD)
        val result = accountCreator.create(client, usdAccountRequest)
        val firstUsdAccount = (result as Created).account

        val secondUsdAccountRequest = Request(1.USD)
        val secondResult = accountCreator.create(client, secondUsdAccountRequest)
        val secondUsdAccount = (secondResult as Created).account

        assertNotEquals(firstUsdAccount, secondUsdAccount)
    }

    @Test
    fun `creating account should insert into repository`() {
        //repository shouldn't contain any accounts for client yet
        val accounts = accountRepository.getAccounts(client)
        assertTrue(accounts.isEmpty())

        val request = Request(1.USD)
        val createdAccount = (accountCreator.create(client, request) as Created).account

        val accountById = accountRepository.getAccount(createdAccount.id)
        assertEquals(createdAccount, accountById)
    }
}


