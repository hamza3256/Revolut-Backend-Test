package customers.accounts

import Customers
import USD
import customers.accounts.transactions.InMemoryTransactionRepository
import customers.accounts.transactions.Transaction
import customers.accounts.transactions.TransactionRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AccountStateQuerierImplTest {

    private lateinit var querier: AccountStateQuerier
    private lateinit var transactionRepository: TransactionRepository

    @Before
    fun beforeEachTest() {
        transactionRepository = InMemoryTransactionRepository()
        querier = AccountStateQuerierImpl(transactionRepository)
    }

    @Test
    fun `account state should show starting balance when no transactions`() {
        val customer = Customers.nikolay()
        val account = Account(0, customer, 100.USD)

        val actualAccountState = querier.getCurrentState(account)
        val expectedAccountState = AccountState(account, 100.USD)
        assertEquals(expectedAccountState, actualAccountState)
    }

    @Test
    fun `account state should show be correct when we have transactions`() {
        val customer = Customers.nikolay()
        val account = Account(0, customer, 100.USD)  //starting balance of $100

        //insert some transactions
        transactionRepository.apply {
            add(Transaction(id = 0, account = account, money = 100.USD)) //after: $200
            add(Transaction(id = 1, account = account, money = 200.USD)) //after: $400
            add(Transaction(id = 2, account = account, money = 600.USD)) //after: $1000
            add(Transaction(id = 3, account = account, money = (-100).USD)) //after: $900
        }

        val actualAccountState = querier.getCurrentState(account)
        val expectedAccountState = AccountState(account, 900.USD)
        assertEquals(expectedAccountState, actualAccountState)
    }

    @Test
    fun `account state should be different before and after adding transactions`() {
        val customer = Customers.nikolay()
        val account = Account(0, customer, 100.USD) //starting balance of $100

        //insert some transactions
        transactionRepository.apply {
            add(Transaction(id = 0, account = account, money = 100.USD)) //after: $200
            add(Transaction(id = 1, account = account, money = 200.USD)) //after: $400
            add(Transaction(id = 2, account = account, money = 600.USD)) //after: $1000
            add(Transaction(id = 3, account = account, money = (-100).USD)) //after: $900
        }

        //account should now have $800
        var accountState = querier.getCurrentState(account)
        var expectedAccountState = AccountState(account, 900.USD)
        assertEquals(expectedAccountState, accountState)

        //add more transactions to verify state changes
        transactionRepository.add(Transaction(id = 4, account = account, money = 100.USD))

        //account should now have $1000
        accountState = querier.getCurrentState(account)
        expectedAccountState = AccountState(account, 1000.USD)
        assertEquals(expectedAccountState, accountState)
    }
}