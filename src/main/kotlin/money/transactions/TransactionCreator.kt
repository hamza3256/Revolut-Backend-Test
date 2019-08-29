package money.transactions

import clients.Client
import money.Money
import money.transactions.TransactionCreator.TransferTransactions
import java.util.concurrent.atomic.AtomicLong

interface TransactionCreator {

    data class Request(val money: Money, val from: Client, val to: Client)

    fun createTransferTransactions(request: Request): TransferTransactions

    data class TransferTransactions(val withdraw: Transaction, val deposit: Transaction)

}

class TransactionCreatorImpl(private val repository: TransactionRepository) : TransactionCreator {

    private val nextId = AtomicLong()

    override fun createTransferTransactions(request: TransactionCreator.Request): TransferTransactions {
        with(request) {
            val fromId = nextId.getAndIncrement()
            val toId = nextId.getAndIncrement()

            val withdraw = Transaction(id = fromId, mirrorTransactionId = toId, money = -money, from = from, to = to)
            val deposit = Transaction(id = toId, mirrorTransactionId = fromId, money = money, from = from, to = to)

            repository.add(withdraw)
            repository.add(deposit)

            return TransferTransactions(withdraw = withdraw, deposit = deposit)
        }
    }
}