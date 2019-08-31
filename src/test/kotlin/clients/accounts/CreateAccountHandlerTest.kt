package clients.accounts

import Clients
import Currencies.USD
import RevolutJavalinConfig
import USD
import UnirestTestConfig
import clients.ClientRepository
import clients.InMemoryClientRepository
import io.javalin.Javalin
import kong.unirest.HttpResponse
import kong.unirest.Unirest
import org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400
import org.eclipse.jetty.http.HttpStatus.OK_200
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.BeforeClass
import org.junit.Test
import clients.accounts.AccountCreator.Request as AccountCreatorRequest

class CreateAccountHandlerTest {

    companion object {

        const val URL = "http://localhost:7000/accounts"

        private lateinit var app: Javalin
        private lateinit var clientRepository: ClientRepository
        private lateinit var accountRepository: AccountRepository
        private lateinit var accountCreator: AccountCreator
        private lateinit var createAccountHandler: CreateAccountHandler

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            UnirestTestConfig.init()

            clientRepository = InMemoryClientRepository()
            accountRepository = InMemoryAccountRepository()
            accountCreator = AccountCreatorImpl(accountRepository)
            createAccountHandler = CreateAccountHandler(
                accountCreator,
                clientRepository
            )

            app = RevolutJavalinConfig.app.start()
            createAccountHandler.attach(app)
        }

        @AfterClass
        @JvmStatic
        fun afterClass() {
            UnirestTestConfig.shutdown()
            app.stop()
        }
    }

    @Test
    fun `should create Account and return created Account for valid request`() {
        val nikolay = Clients.nikolay(0)
        clientRepository.addClient(nikolay)

        //shouldn't have a USD account yet
        assertNull(accountRepository.getAccount(nikolay, USD))

        val response: HttpResponse<Account> = postAsObject(
            body = AccountCreatorRequest(100.USD),
            clientId = "0"
        )

        assertEquals(OK_200, response.status)
        val expectedAccount = Account(0, nikolay, 100.USD)
        assertEquals(expectedAccount, response.body)
    }

    @Test
    fun `should return error when clientId missing`() {
        val response = postAsString(
            body = AccountCreatorRequest(100.USD)
            //missing clientId
        )

        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should return error when clientId isn't a number`() {
        val response = postAsString(
            body = AccountCreatorRequest(100.USD),
            clientId = "abc" //must be a number
        )

        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should return error when clientId isn't present in repository`() {
        val response = postAsString(
            body = AccountCreatorRequest(100.USD),
            clientId = "1000" //no such Client with id 1000
        )

        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should return error when invalid body given`(){
        val response = postAsString(
            body = "a weird body", //not a valid AccountCreatorRequest
            clientId = "0"
        )

        assertEquals(BAD_REQUEST_400, response.status)
    }

    private fun postAsString(
        body: Any? = null,
        clientId: String? = null
    ): HttpResponse<String> {
        return Unirest.post(URL)
            .body(body)
            .let { request ->
                //only add clientId as query string if it's not null
                clientId?.let { request.queryString("clientId", it).asString() } ?: request.asString()
            }
    }

    private inline fun <reified T> postAsObject(
        body: Any? = null,
        clientId: String? = null
    ): HttpResponse<T> {
        return Unirest.post(URL)
            .body(body)
            .let { request ->
                clientId?.let { request.queryString("clientId", it).asObject(T::class.java) }
                    ?: request.asObject(T::class.java)
            }
    }
}
