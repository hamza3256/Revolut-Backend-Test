package customers.accounts

import Customers
import RevolutConfig
import USD
import UnirestTestConfig
import customers.CustomerRepository
import customers.InMemoryCustomerRepository
import customers.accounts.CreateAccount.RequestBody
import customers.accounts.CreateAccount.ResponseBody
import io.javalin.Javalin
import kong.unirest.Unirest
import org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400
import org.eclipse.jetty.http.HttpStatus.OK_200
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test

class CreateAccountHandlerTest {

    companion object {

        private lateinit var javalin: Javalin
        private lateinit var customerRepository: CustomerRepository
        private lateinit var accountRepository: AccountRepository
        private lateinit var accountCreator: AccountCreator
        private lateinit var createAccountHandler: CreateAccountHandler

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            val revolutConfig = RevolutConfig()
            UnirestTestConfig.init(revolutConfig.objectMapper)

            customerRepository = InMemoryCustomerRepository()
            accountRepository = InMemoryAccountRepository()
            accountCreator = AccountCreatorImpl(accountRepository)
            createAccountHandler = CreateAccountHandler(
                accountCreator,
                customerRepository
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
        val nikolay = Customers.nikolay(0)
        customerRepository.addCustomer(nikolay)

        //shouldn't have any accounts yet
        assertTrue(accountRepository.getAccounts(nikolay).isEmpty())

        val response = post(customerIdStr = nikolay.id.toString())
            .body(RequestBody(startingMoney = 100.USD))
            .asObject(ResponseBody::class.java)

        assertEquals(OK_200, response.status)
        val expectedAccount = Account(0, nikolay, 100.USD)
        assertEquals(expectedAccount, response.body.account)
    }

    @Test
    fun `should return error when customerId isn't present in repository`() {
        val response = post(customerIdStr = "1000")
            .body(RequestBody(startingMoney = 100.USD))
            .asString()

        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should return error when customerId isn't a Long`() {
        val response = post(customerIdStr = "notAlong")
            .body(RequestBody(startingMoney = 100.USD))
            .asString()

        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should return error when invalid body given`() {
        val response = post("0")
            .body("a weird body") //not a valid CreateAccount.RequestBody
            .asString()

        assertEquals(BAD_REQUEST_400, response.status)
    }

    private fun post(customerIdStr: String) = Unirest.post("http://localhost:7000/customers/$customerIdStr/accounts")
}
