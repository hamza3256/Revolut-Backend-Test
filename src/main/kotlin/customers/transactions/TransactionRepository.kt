package customers.transactions

import customers.Customer
import customers.accounts.Account

interface TransactionRepository {

    /**
     * Returns a set of all transactions across all of the accounts of a [customer]
     * */
    fun getAll(customer: Customer): Set<Transaction>

    /**
     * Returns a set of all transactions belonging to a given [account]
     * */
    fun getAll(account: Account): Set<Transaction>

    /**
     * Adds a transaction.
     * @return true if such a transaction didn't exist and was inserted, or false when a transaction with the given id exists and wasn't inserted
     * */
    fun add(transaction: Transaction): Boolean

    /**
     * Delete all transactions.
     * */
    fun deleteAll()

}

class InMemoryTransactionRepository : TransactionRepository {

    private val idsToTransactions: MutableMap<Long, Transaction> = mutableMapOf()

    override fun getAll(customer: Customer): Set<Transaction> {
        return idsToTransactions.values.filter { it.account.customer == customer }.toSet()
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