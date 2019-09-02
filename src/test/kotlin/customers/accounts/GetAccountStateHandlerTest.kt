package customers.accounts

import Currencies.USD
import Customers
import RevolutConfig
import USD
import UnirestTestConfig
import customers.accounts.GetAccount.ResponseBody
import customers.accounts.transactions.InMemoryTransactionRepository
import io.javalin.Javalin
import kong.unirest.GetRequest
import kong.unirest.Unirest
import org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400
import org.eclipse.jetty.http.HttpStatus.OK_200
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.BeforeClass
import org.junit.Test

class GetAccountStateHandlerTest {

    companion object {

        private lateinit var app: Javalin
        private lateinit var accountStateQuerier: AccountStateQuerier
        private lateinit var accountRepository: AccountRepository

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            val config = RevolutConfig()
            UnirestTestConfig.init(config.objectMapper)

            accountRepository = InMemoryAccountRepository()
            val transactionRepository = InMemoryTransactionRepository()
            accountStateQuerier = AccountStateQuerierImpl(transactionRepository)
            app = config.javalin
            GetAccountStateHandler(accountRepository, accountStateQuerier).attach(app)

            app.start()
        }

        @AfterClass
        @JvmStatic
        fun afterClass() {
            Unirest.shutDown()
            app.stop()
        }
    }

    @Test
    fun `should return account state when all requirements met`() {
        val customer = Customers.nikolay()
        val account = Account(0, customer, USD)
        accountRepository.addAccount(account)

        val response = get("0").asObject(ResponseBody::class.java)

        assertEquals(OK_200, response.status)
        val expectedResponseBody = ResponseBody(
            accountState = AccountState(
                account = account,
                money = 0.USD
            )
        )
        assertEquals(expectedResponseBody, response.body)
    }

    @Test
    fun `should return error when id isn't a valid Long`() {
        val response = get("notAValidLong").asString()

        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should return error when account for id doesn't exist`() {
        assertNull(accountRepository.getAccount(1000)) //Account doesn't exist for given id

        val response = get("1000").asString()

        assertEquals(BAD_REQUEST_400, response.status)
    }

    private fun get(accountIdPathParam: String): GetRequest {
        return Unirest.get("http://localhost:7000/accounts/$accountIdPathParam/state")
    }
}