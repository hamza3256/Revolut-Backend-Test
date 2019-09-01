package clients.accounts

import Clients
import RevolutConfig
import USD
import UnirestTestConfig
import clients.ClientRepository
import clients.InMemoryClientRepository
import clients.accounts.CreateAccount.RequestBody
import clients.accounts.CreateAccount.ResponseBody
import io.javalin.Javalin
import kong.unirest.Unirest
import org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400
import org.eclipse.jetty.http.HttpStatus.OK_200
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test

class CreateAccountHandlerTest {

    companion object {

        const val URL = "http://localhost:7000/accounts"

        private lateinit var javalin: Javalin
        private lateinit var clientRepository: ClientRepository
        private lateinit var accountRepository: AccountRepository
        private lateinit var accountCreator: AccountCreator
        private lateinit var createAccountHandler: CreateAccountHandler

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            val revolutConfig = RevolutConfig()
            UnirestTestConfig.init(revolutConfig.objectMapper)

            clientRepository = InMemoryClientRepository()
            accountRepository = InMemoryAccountRepository()
            accountCreator = AccountCreatorImpl(accountRepository)
            createAccountHandler = CreateAccountHandler(
                accountCreator,
                clientRepository
            )

            javalin = revolutConfig.javalin.start()
            createAccountHandler.attach(javalin)
        }

        @AfterClass
        @JvmStatic
        fun afterClass() {
            UnirestTestConfig.shutdown()
            javalin.stop()
        }
    }

    @Test
    fun `should create Account and return created Account for valid request`() {
        val nikolay = Clients.nikolay(0)
        clientRepository.addClient(nikolay)

        //shouldn't have any accounts yet
        assertTrue(accountRepository.getAccounts(nikolay).isEmpty())

        val response = post()
            .body(RequestBody(clientId = 0, startingMoney = 100.USD))
            .asObject(ResponseBody::class.java)

        assertEquals(OK_200, response.status)
        val expectedAccount = Account(0, nikolay, 100.USD)
        assertEquals(expectedAccount, response.body.account)
    }

    @Test
    fun `should return error when clientId isn't present in repository`() {
        val response = post()
            .body(RequestBody(clientId = 1000, startingMoney = 100.USD))
            .asString()

        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should return error when invalid body given`() {
        val response = post()
            .body("a weird body") //not a valid CreateAccount.RequestBody
            .asString()

        assertEquals(BAD_REQUEST_400, response.status)
    }

    private fun post() = Unirest.post(URL)
}
