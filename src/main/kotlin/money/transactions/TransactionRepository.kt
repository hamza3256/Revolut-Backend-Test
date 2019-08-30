package money.transactions

import clients.Client
import clients.accounts.Account

interface TransactionRepository {

    fun getAll(client: Client): List<Transaction>
    fun getAll(account: Account): List<Transaction>
    fun add(transaction: Transaction)

}

class InMemoryTransactionRepository : TransactionRepository {
    private val transactions: MutableSet<Transaction> = mutableSetOf()

    override fun getAll(client: Client): List<Transaction> {
        return transactions.filter { it.account.client == client }
    }

    override fun getAll(account: Account): List<Transaction> {
        return transactions.filter { it.account == account }
    }

    override fun add(transaction: Transaction) {
        transactions.add(transaction)
    }
}