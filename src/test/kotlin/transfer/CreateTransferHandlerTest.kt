package transfer

import Customers
import RevolutConfig
import USD
import UnirestTestConfig
import customers.CustomerRepository
import customers.InMemoryCustomerRepository
import customers.accounts.*
import customers.accounts.transactions.InMemoryTransactionRepository
import customers.accounts.transactions.TransactionCreatorImpl
import customers.accounts.transactions.TransactionRepository
import io.javalin.Javalin
import kong.unirest.HttpRequestWithBody
import kong.unirest.Unirest
import org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400
import org.eclipse.jetty.http.HttpStatus.OK_200
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import transfer.CreateTransfer.RequestBody
import transfer.CreateTransfer.ResponseBody

class CreateTransferHandlerTest {

    companion object {

        const val URL = "http://localhost:7000/transfers"

        private lateinit var javalin: Javalin

        private lateinit var transactionRepository: TransactionRepository
        private lateinit var customerRepository: CustomerRepository
        private lateinit var accountRepository: AccountRepository
        private lateinit var createTransferHandler: CreateTransferHandler

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            val revolutConfig = RevolutConfig()
            UnirestTestConfig.init(revolutConfig.objectMapper)

            transactionRepository = InMemoryTransactionRepository()
            val transactionCreator = TransactionCreatorImpl(transactionRepository)
            val accountStateQuerier = AccountStateQuerierImpl(transactionRepository)

            customerRepository = InMemoryCustomerRepository()
            accountRepository = InMemoryAccountRepository()

            val transferer = MoneyTransfererImpl(transactionCreator, accountStateQuerier)
            createTransferHandler = CreateTransferHandler(accountRepository, transferer)

            javalin = revolutConfig.javalin.start()
            createTransferHandler.attach(javalin)
        }

        @AfterClass
        @JvmStatic
        fun afterClass() {
            UnirestTestConfig.shutdown()
            javalin.stop()
        }
    }

    @Before
    fun beforeEachTest() {
        transactionRepository.deleteAll()
        accountRepository.deleteAll()
        customerRepository.deleteAll()
    }

    @Test
    fun `should correctly transfer money between 2 different customers when all requirements met`() {
        //create nikolay with an account containing 1000USD
        val nikolay = Customers.nikolay(0)
        customerRepository.addCustomer(nikolay)
        val nikolaysUsdAccount = Account(0, nikolay, 1000.USD)
        accountRepository.addAccount(nikolaysUsdAccount)

        //create vlad with an account containing 1000USD
        val vlad = Customers.vlad(1)
        customerRepository.addCustomer(vlad)
        val vladsUsdAccount = Account(1, vlad, 1000.USD)
        accountRepository.addAccount(vladsUsdAccount)

        //transfer $500 from nikolay to vlad
        val body = RequestBody(
            fromAccountId = nikolaysUsdAccount.id,
            toAccountId = vladsUsdAccount.id,
            money = 500.USD
        )
        val response = post()
            .body(body)
            .asObject(ResponseBody::class.java)

        assertEquals(OK_200, response.status)
        val expectedResponseBody = ResponseBody(transaction = transactionRepository.getAll(nikolaysUsdAccount).first())
        assertEquals(expectedResponseBody, response.body)
    }

    @Test
    fun `should correctly transfer money between two accounts of the same Customer`() {
        //create nikolay with 2 accounts
        val nikolay = Customers.nikolay(0)
        customerRepository.addCustomer(nikolay)

        //first has $1000
        val fromAccount = Account(0, nikolay, 1000.USD)
        accountRepository.addAccount(fromAccount)

        //second has $3000
        val toAccount = Account(1, nikolay, 3000.USD)
        accountRepository.addAccount(toAccount)

        //transfer $500 from first to second
        val body = RequestBody(
            fromAccountId = fromAccount.id,
            toAccountId = toAccount.id,
            money = 500.USD
        )
        val response = post()
            .body(body)
            .asObject(ResponseBody::class.java)

        val expectedResponseBody = ResponseBody(transaction = transactionRepository.getAll(fromAccount).first())
        assertEquals(expectedResponseBody, response.body)
    }

    @Test
    fun `should fail when fromAccount not found for id`() {
        val nikolay = Customers.nikolay(0)
        customerRepository.addCustomer(nikolay)
        val nikolaysAccount = Account(0, nikolay, 100.USD)
        accountRepository.addAccount(nikolaysAccount)

        assertNull(accountRepository.getAccount(1000)) //should not exist
        val body = RequestBody(
            fromAccountId = 1000,
            toAccountId = nikolaysAccount.id,
            money = 100.USD
        )

        val response = post().body(body).asString()
        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should fail when toAccount not found for id`() {
        val nikolay = Customers.nikolay(0)
        customerRepository.addCustomer(nikolay)
        val nikolaysAccount = Account(0, nikolay, 100.USD)
        accountRepository.addAccount(nikolaysAccount)

        assertNull(accountRepository.getAccount(1000)) //should not exist
        val body = RequestBody(
            fromAccountId = nikolaysAccount.id,
            toAccountId = 1000,
            money = 100.USD
        )

        val response = post().body(body).asString()
        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should fail when attempting to transfer between same account`() {
        val nikolay = Customers.nikolay(0)
        customerRepository.addCustomer(nikolay)
        val account = Account(0, nikolay, 100.USD)
        accountRepository.addAccount(account)

        val body = RequestBody(
            fromAccountId = account.id,
            toAccountId = account.id,
            money = 100.USD
        )

        val response = post().body(body).asString()

        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should fail when attempting to transfer negative funds`() {
        val nikolay = Customers.nikolay(0)
        customerRepository.addCustomer(nikolay)
        val nikolaysUsdAccount = Account(0, nikolay, 100.USD)
        accountRepository.addAccount(nikolaysUsdAccount)

        val vlad = Customers.vlad(1)
        customerRepository.addCustomer(vlad)
        val vladsUsdAccount = Account(1, vlad, 100.USD)
        accountRepository.addAccount(vladsUsdAccount)

        val body = RequestBody(
            fromAccountId = nikolaysUsdAccount.id,
            toAccountId = vladsUsdAccount.id,
            money = (-100).USD
        )

        val response = post().body(body).asString()

        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should fail when attempting to transfer too much money`() {
        val nikolay = Customers.nikolay(0)
        customerRepository.addCustomer(nikolay)
        val nikolaysUsdAccount = Account(0, nikolay, 100.USD)
        accountRepository.addAccount(nikolaysUsdAccount)

        val vlad = Customers.vlad(1)
        customerRepository.addCustomer(vlad)
        val vladsUsdAccount = Account(1, vlad, 100.USD)
        accountRepository.addAccount(vladsUsdAccount)

        val body = RequestBody(
            fromAccountId = nikolaysUsdAccount.id,
            toAccountId = vladsUsdAccount.id,
            money = 99999.USD //nikolay doesn't have that much money
        )

        val response = post().body(body).asString()

        assertEquals(BAD_REQUEST_400, response.status)
    }

    private fun post(): HttpRequestWithBody = Unirest.post(URL)
}