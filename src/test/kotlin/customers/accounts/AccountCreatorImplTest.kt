package customers.accounts

import Customers
import Currencies.GBP
import Currencies.USD
import GBP
import USD
import customers.accounts.AccountCreator.Request
import customers.accounts.AccountCreator.Result.Created
import customers.accounts.AccountCreator.Result.NegativeMoney
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AccountCreatorImplTest {

    private lateinit var accountRepository: AccountRepository
    private lateinit var accountCreator: AccountCreatorImpl

    private val customer = Customers.nikolay()

    @Before
    fun beforeEachTest() {
        accountRepository = InMemoryAccountRepository()
        accountCreator = AccountCreatorImpl(accountRepository)
    }

    @Test
    fun `creating account for Customer with no accounts should succeed`() {
        val usdAccountRequest = Request(0.USD)
        val result = accountCreator.create(customer, usdAccountRequest)

        assert(result is Created)
    }

    @Test
    fun `created account should have correct currency`() {
        val usdAccountRequest = Request(0.USD)
        val gbpAccountRequest = Request(0.GBP)

        val usdResult = accountCreator.create(customer, usdAccountRequest) as Created
        val gbpResult = accountCreator.create(customer, gbpAccountRequest) as Created

        assertEquals(USD, usdResult.account.currency)
        assertEquals(GBP, gbpResult.account.currency)
    }

    @Test
    fun `creating account with starting money should return Account with correct starting money`() {
        val usdAccountRequest = Request(1000.USD)
        val result = accountCreator.create(customer, usdAccountRequest) as Created

        assertEquals(1000.USD, result.account.startingMoney)
    }

    @Test
    fun `creating account with zero money should succeed`() {
        val request = Request(0.USD)
        val result = accountCreator.create(customer, request)

        assertTrue(result is Created)
    }

    @Test
    fun `creating account with negative money should fail`() {
        val request = Request((-1).USD)
        val result = accountCreator.create(customer, request)

        assertTrue(result is NegativeMoney)
    }

    @Test
    fun `creating account with same currency should succeed`() {
        val usdAccountRequest = Request(1.USD)
        accountCreator.create(customer, usdAccountRequest)

        val secondUsdAccountRequest = Request(2.USD)
        val result = accountCreator.create(customer, secondUsdAccountRequest)

        assertTrue(result is Created)
    }

    @Test
    fun `creating account with same currency shouldn't overwrite previous account`() {
        val usdAccountRequest = Request(1.USD)
        val result = accountCreator.create(customer, usdAccountRequest)
        val firstUsdAccount = (result as Created).account

        val secondUsdAccountRequest = Request(1.USD)
        val secondResult = accountCreator.create(customer, secondUsdAccountRequest)
        val secondUsdAccount = (secondResult as Created).account

        assertNotEquals(firstUsdAccount, secondUsdAccount)
    }

    @Test
    fun `creating account should insert into repository`() {
        //repository shouldn't contain any accounts for customer yet
        val accounts = accountRepository.getAccounts(customer)
        assertTrue(accounts.isEmpty())

        val request = Request(1.USD)
        val createdAccount = (accountCreator.create(customer, request) as Created).account

        val accountById = accountRepository.getAccount(createdAccount.id)
        assertEquals(createdAccount, accountById)
    }
}


