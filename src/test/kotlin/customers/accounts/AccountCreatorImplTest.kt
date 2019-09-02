package customers.accounts

import Currencies.GBP
import Currencies.USD
import Customers
import USD
import customers.accounts.AccountCreator.Request
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
        val usdAccountRequest = Request(USD)

        val account = accountCreator.create(customer, usdAccountRequest)
        val expectedAccount = Account(0, customer, USD)
        assertEquals(expectedAccount, account)
    }

    @Test
    fun `created account should have correct currency`() {
        val usdAccountRequest = Request(USD)
        val gbpAccountRequest = Request(GBP)

        val usdAccount = accountCreator.create(customer, usdAccountRequest)
        val gbpAccount = accountCreator.create(customer, gbpAccountRequest)

        assertEquals(USD, usdAccount.currency)
        assertEquals(GBP, gbpAccount.currency)
    }

    @Test
    fun `creating account with same currency shouldn't overwrite previous account`() {
        val usdAccountRequest = Request(USD)
        val firstUsdAccount = accountCreator.create(customer, usdAccountRequest)

        val secondUsdAccountRequest = Request(USD)
        val secondUsdAccount = accountCreator.create(customer, secondUsdAccountRequest)

        assertNotEquals(firstUsdAccount, secondUsdAccount)
    }

    @Test
    fun `creating account should insert into repository`() {
        //repository shouldn't contain any accounts for customer yet
        val accounts = accountRepository.getAccounts(customer)
        assertTrue(accounts.isEmpty())

        val request = Request(USD)
        val createdAccount = accountCreator.create(customer, request)

        val accountById = accountRepository.getAccount(createdAccount.id)
        assertEquals(createdAccount, accountById)
    }
}


