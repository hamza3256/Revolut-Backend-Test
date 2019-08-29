package money.transactions

import clients.Client

interface TransactionRepository {

    fun getAll(client: Client): List<Transaction>
    fun add(transaction: Transaction)

}

class InMemoryTransactionRepository : TransactionRepository {
    private val transactions: MutableSet<Transaction> = mutableSetOf()

    override fun getAll(client: Client): List<Transaction> {
        return transactions.filter { it.to == client }
    }

    override fun add(transaction: Transaction) {
        transactions.add(transaction)
    }
}