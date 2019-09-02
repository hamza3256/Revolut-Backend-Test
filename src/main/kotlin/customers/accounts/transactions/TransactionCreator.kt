package customers.accounts.transactions

import customers.accounts.Account
import money.Money
import java.util.concurrent.atomic.AtomicLong

/**
 * Used to create new Transactions, such as transfers between accounts.
 * */
interface TransactionCreator {

    /**
     * The request holds information on how much money is being transferred and from/to who.
     * */
    data class Request(val money: Money, val from: Account, val to: Account)

    /**
     * Create and persists Transactions for the given [request]
     * */
    fun createTransferTransactions(request: Request)

}

class TransactionCreatorImpl(private val repository: TransactionRepository) : TransactionCreator {

    private val nextId = AtomicLong()

    override fun createTransferTransactions(request: TransactionCreator.Request) {
        with(request) {
            val fromId = nextId.getAndIncrement()
            val toId = nextId.getAndIncrement()

            val withdraw = Transaction(id = fromId, mirrorTransactionId = toId, money = -money, account = from)
            val deposit = Transaction(id = toId, mirrorTransactionId = fromId, money = money, account = to)

            repository.add(withdraw)
            repository.add(deposit)
        }
    }
}