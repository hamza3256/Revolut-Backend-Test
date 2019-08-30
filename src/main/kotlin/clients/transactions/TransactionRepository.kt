package clients.transactions

import clients.Client
import clients.accounts.Account

interface TransactionRepository {

    /**
     * Returns a list of all transactions across all of the [client] accounts
     * */
    fun getAll(client: Client): List<Transaction>

    /**
     * Returns a list of all transactions for a given [account]
     * */
    fun getAll(account: Account): List<Transaction>

    /**
     * Adds a transaction.
     * @return true if such a transaction didn't exist and was inserted, or false when it already exists and wasn't inserted
     * */
    fun add(transaction: Transaction): Boolean

}

class InMemoryTransactionRepository : TransactionRepository {
    private val transactions: MutableSet<Transaction> = mutableSetOf()

    override fun getAll(client: Client): List<Transaction> {
        return transactions.filter { it.account.client == client }
    }

    override fun getAll(account: Account): List<Transaction> {
        return transactions.filter { it.account == account }
    }

    override fun add(transaction: Transaction): Boolean {
        return transactions.add(transaction)
    }
}