package transfer

import Clients
import RevolutConfig
import USD
import UnirestTestConfig
import clients.ClientRepository
import clients.InMemoryClientRepository
import clients.accounts.*
import clients.transactions.InMemoryTransactionRepository
import clients.transactions.TransactionCreatorImpl
import clients.transactions.TransactionRepository
import io.javalin.Javalin
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
        private lateinit var clientRepository: ClientRepository
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

            clientRepository = InMemoryClientRepository()
            accountRepository = InMemoryAccountRepository()

            val transferer = MoneyTransfererImpl(accountRepository, transactionCreator, accountStateQuerier)
            createTransferHandler = CreateTransferHandler(clientRepository, transferer)

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
        clientRepository.deleteAll()
    }

    @Test
    fun `should correctly transfer money when all requirements met`() {
        //create nikolay with an account containing 1000USD
        val nikolay = Clients.nikolay(0)
        clientRepository.addClient(nikolay)
        val nikolaysUsdAccount = Account(0, nikolay, 1000.USD)
        accountRepository.addAccount(nikolaysUsdAccount)

        //create vlad with an account containing 1000USD
        val vlad = Clients.vlad(1)
        clientRepository.addClient(vlad)
        val vladsUsdAccount = Account(1, vlad, 1000.USD)
        accountRepository.addAccount(vladsUsdAccount)

        //transfer $500 from nikolay to vlad
        val body = RequestBody(
            fromClientId = nikolay.id,
            toClientId = vlad.id,
            money = 500.USD
        )
        val response = post()
            .body(body)
            .asObject(ResponseBody::class.java)

        assertEquals(OK_200, response.status)
        val expectedResponseBody = ResponseBody(
            fromAccountState = AccountState(
                account = nikolaysUsdAccount,
                money = 500.USD //should have $1000 - $500 = $500 after transferring
            ),
            toAccountState = AccountState(
                account = vladsUsdAccount,
                money = 1500.USD //should have $1000 + $500 = $1500 after transferring
            )
        )
        assertEquals(expectedResponseBody, response.body)
    }

    @Test
    fun `should fail when fromClient not found for id`() {
        val nikolay = Clients.nikolay(0)
        clientRepository.addClient(nikolay)

        assertNull(clientRepository.getClient(1000)) //should not exist
        val body = RequestBody(
            fromClientId = 1000,
            toClientId = nikolay.id,
            money = 100.USD
        )

        val response = post().body(body).asString()
        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should fail when toClient not found for id`() {
        val nikolay = Clients.nikolay(0)
        clientRepository.addClient(nikolay)

        assertNull(clientRepository.getClient(1000)) //should not exist
        val body = RequestBody(
            fromClientId = nikolay.id,
            toClientId = 1000,
            money = 100.USD
        )

        val response = post().body(body).asString()
        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should fail when attempting to transfer between same client`() {
        val nikolay = Clients.nikolay(0)
        clientRepository.addClient(nikolay)
        accountRepository.addAccount(Account(0, nikolay, 100.USD))

        val body = RequestBody(
            fromClientId = nikolay.id,
            toClientId = nikolay.id,
            money = 100.USD
        )

        val response = post().body(body).asString()

        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should fail when attempting to transfer negative funds`() {
        val nikolay = Clients.nikolay(0)
        clientRepository.addClient(nikolay)
        val nikolaysUsdAccount = Account(0, nikolay, 100.USD)
        accountRepository.addAccount(nikolaysUsdAccount)

        val vlad = Clients.vlad(1)
        clientRepository.addClient(vlad)
        val vladsUsdAccount = Account(1, vlad, 100.USD)
        accountRepository.addAccount(vladsUsdAccount)

        val body = RequestBody(
            fromClientId = nikolay.id,
            toClientId = vlad.id,
            money = (-100).USD
        )

        val response = post().body(body).asString()

        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should fail when attempting to transfer too much money`(){
        val nikolay = Clients.nikolay(0)
        clientRepository.addClient(nikolay)
        val nikolaysUsdAccount = Account(0, nikolay, 100.USD)
        accountRepository.addAccount(nikolaysUsdAccount)

        val vlad = Clients.vlad(1)
        clientRepository.addClient(vlad)
        val vladsUsdAccount = Account(1, vlad, 100.USD)
        accountRepository.addAccount(vladsUsdAccount)

        val body = RequestBody(
            fromClientId = nikolay.id,
            toClientId = vlad.id,
            money = 99999.USD //nikolay doesn't have that much money
        )

        val response = post().body(body).asString()

        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should fail when fromClient doesn't have an account for the given currency`(){
        //nikolay doesn't have any accounts
        val nikolay = Clients.nikolay(0)
        clientRepository.addClient(nikolay)

        val vlad = Clients.vlad(1)
        clientRepository.addClient(vlad)
        val vladsUsdAccount = Account(1, vlad, 100.USD)
        accountRepository.addAccount(vladsUsdAccount)

        val body = RequestBody(
            fromClientId = nikolay.id,
            toClientId = vlad.id,
            money = 10.USD //nikolay doesn't have a USD account
        )

        val response = post().body(body).asString()

        assertEquals(BAD_REQUEST_400, response.status)
    }

    @Test
    fun `should fail when toClient doesn't have an account to accept funds`(){
        val nikolay = Clients.nikolay(0)
        clientRepository.addClient(nikolay)
        val nikolaysUsdAccount = Account(0, nikolay, 100.USD)
        accountRepository.addAccount(nikolaysUsdAccount)

        //vlad doesn't have a USD account to accept transfer
        val vlad = Clients.vlad(1)
        clientRepository.addClient(vlad)

        val body = RequestBody(
            fromClientId = nikolay.id,
            toClientId = vlad.id,
            money = 10.USD
        )

        val response = post().body(body).asString()

        assertEquals(BAD_REQUEST_400, response.status)
    }

    fun post() = Unirest.post(URL)
}