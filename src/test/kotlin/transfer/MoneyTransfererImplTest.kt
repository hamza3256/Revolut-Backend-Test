package transfer

import Currencies.USD
import USD
import clients.Client
import money.account.*
import money.transactions.InMemoryTransactionRepository
import money.transactions.TransactionCreator
import money.transactions.TransactionCreatorImpl
import money.transactions.TransactionRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import transfer.TransferResult.*

class MoneyTransfererImplTest {

    private lateinit var moneyTransferer: MoneyTransferer
    private lateinit var accountRepository: AccountRepository
    private lateinit var transactionCreator: TransactionCreator
    private lateinit var accountStateQuerier: AccountStateQuerier
    private lateinit var transactionRepository: TransactionRepository

    @Before
    fun beforeEachTest() {
        accountRepository = InMemoryAccountRepository()
        transactionRepository = InMemoryTransactionRepository()
        accountStateQuerier = AccountStateQuerierImpl(transactionRepository)
        transactionCreator = TransactionCreatorImpl(transactionRepository)
        moneyTransferer = MoneyTransfererImpl(accountRepository, transactionCreator, accountStateQuerier)
    }

    @Test
    fun `transfering money from client without an account for the given currency should fail`() {
        val vlad = Client(0, "Vlad", "Yatsenko")
        val nikolay = Client(1, "Nikolay", "Storonsky")

        val result = moneyTransferer.transferMoney(10.USD, from = vlad, to = nikolay)
        assertTrue(result is MissingAccount)
    }

    @Test
    fun `transfering zero money from client without an account for the given currency should succeed`() {
        val vlad = Client(0, "Vlad", "Yatsenko")
        val nikolay = Client(1, "Nikolay", "Storonsky")

        val result = moneyTransferer.transferMoney(0.USD, from = vlad, to = nikolay)
        assertTrue(result is Success)
    }

    @Test
    fun `transfering between same account shouldn't create a transaction`() {
        val vlad = Client(0, "Vlad", "Yatsenko")
        val account = Account(0, vlad, 1000.USD)
        accountRepository.addAccount(vlad, account)

        val result = moneyTransferer.transferMoney(10.USD, from = vlad, to = vlad)
        assertEquals(SameAccount, result)

        //check no transactions - we could have also used Mockito's `verifyNoMoreInteractions` were the object mocked
        assertTrue(transactionRepository.getAll(account).isEmpty())
    }

    @Test
    fun `transfering money to client who doesn't have an account for that currency should return MissingAccount`() {
        //vlad has $1000
        val vlad = Client(0, "Vlad", "Yatsenko")
        val vladsUsdAccount = Account(0, vlad, 1000.USD)
        accountRepository.addAccount(vlad, vladsUsdAccount)

        //nikolay doesn't have a USD account
        val nikolay = Client(1, "Nikolay", "Storonsky")
        assertNull(accountRepository.getAccount(nikolay, USD))

        //transfer $10 from vlad to nikolay
        val result = moneyTransferer.transferMoney(10.USD, from = vlad, to = nikolay)
        assertTrue(result is MissingAccount)
        result as MissingAccount

        //nikolay doesn't have a USD account
        assertEquals(nikolay, result.client)
    }

    @Test
    fun `transfering too much money should fail`() {
        //vlad has $1000
        val vlad = Client(0, "Vlad", "Yatsenko")
        val vladsUsdAccount = Account(0, vlad, 1000.USD)
        accountRepository.addAccount(vlad, vladsUsdAccount)

        //nikolay has a USD account to accept the transfer
        val nikolay = Client(1, "Nikolay", "Storonsky")
        val nikolaysUsdAccount = Account(1, nikolay, USD)
        accountRepository.addAccount(nikolay, nikolaysUsdAccount)

        //transfer $1001 from vlad to nikolay
        val result = moneyTransferer.transferMoney(1001.USD, from = vlad, to = nikolay)
        assertTrue(result is InsufficientFunds)
    }

    @Test
    fun `transferring entire accounts funds should succeed`() {
        //vlad has $1000
        val vlad = Client(0, "Vlad", "Yatsenko")
        val vladsUsdAccount = Account(0, vlad, 1000.USD)
        accountRepository.addAccount(vlad, vladsUsdAccount)

        //nikolay has a USD account to accept the transfer
        val nikolay = Client(1, "Nikolay", "Storonsky")
        val nikolaysUsdAccount = Account(1, nikolay, USD)
        accountRepository.addAccount(nikolay, nikolaysUsdAccount)

        //transfer all of vlads USD money to nikolay
        val result = moneyTransferer.transferMoney(from = vlad, to = nikolay, money = 1000.USD)
        assertTrue(result is Success)
    }

    @Test
    fun `transferring money should deduct from senders account`() {
        //vlad has $1000
        val vlad = Client(0, "Vlad", "Yatsenko")
        val vladsUsdAccount = Account(0, vlad, 1000.USD)
        accountRepository.addAccount(vlad, vladsUsdAccount)

        //nikolay has a USD account to accept the transfer
        val nikolay = Client(1, "Nikolay", "Storonsky")
        val nikolaysUsdAccount = Account(1, nikolay, USD)
        accountRepository.addAccount(nikolay, nikolaysUsdAccount)

        //transfer $500 from vlad to nikolay
        val result = moneyTransferer.transferMoney(500.USD, from = vlad, to = nikolay)
        assertTrue(result is Success)

        //vlad should now have $500
        val vladsUsdAccountState = accountStateQuerier.getCurrentState(vladsUsdAccount)
        assertEquals(500.USD, vladsUsdAccountState.money)
    }

    @Test
    fun `transferring money should add money to targets account`() {
        //vlad has $1000
        val vlad = Client(0, "Vlad", "Yatsenko")
        val vladsUsdAccount = Account(0, vlad, 1000.USD)
        accountRepository.addAccount(vlad, vladsUsdAccount)

        //nikolay has a USD account to accept the transfer
        val nikolay = Client(1, "Nikolay", "Storonsky")
        val nikolaysUsdAccount = Account(1, nikolay, USD)
        accountRepository.addAccount(nikolay, nikolaysUsdAccount)

        //transfer $500 from vlad to nikolay
        val result = moneyTransferer.transferMoney(500.USD, from = vlad, to = nikolay)
        assertTrue(result is Success)

        //nikolay should now have $500
        val nikolaysUsdAccountState = accountStateQuerier.getCurrentState(nikolaysUsdAccount)
        assertEquals(500.USD, nikolaysUsdAccountState.money)
    }
}