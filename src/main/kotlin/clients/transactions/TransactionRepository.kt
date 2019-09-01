package clients.transactions

import clients.Client
import clients.accounts.Account

interface TransactionRepository {

    /**
     * Returns a set of all transactions across all of the accounts of a [client]
     * */
    fun getAll(client: Client): Set<Transaction>

    /**
     * Returns a set of all transactions belonging to a given [account]
     * */
    fun getAll(account: Account): Set<Transaction>

    /**
     * Adds a transaction.
     * @return true if such a transaction didn't exist and was inserted, or false when a transaction with the given id exists and wasn't inserted
     * */
    fun add(transaction: Transaction): Boolean

    //TODO test
    fun deleteAll()

}

class InMemoryTransactionRepository : TransactionRepository {

    private val idsToTransactions: MutableMap<Long, Transaction> = mutableMapOf()

    override fun getAll(client: Client): Set<Transaction> {
        return idsToTransactions.values.filter { it.account.client == client }.toSet()
    }

    override fun getAll(account: Account): Set<Transaction> {
        return idsToTransactions.values.filter { it.account == account }.toSet()
    }

    override fun add(transaction: Transaction): Boolean {
        return if (transaction.id in idsToTransactions) {
            false
        } else {
            idsToTransactions[transaction.id] = transaction
            true
        }
    }

    override fun deleteAll() = idsToTransactions.clear()
}