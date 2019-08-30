package clients.transactions

import money.Money
import clients.accounts.Account
import clients.transactions.TransactionCreator.TransferTransactions
import java.util.concurrent.atomic.AtomicLong

interface TransactionCreator {

    data class Request(val money: Money, val from: Account, val to: Account)

    fun createTransferTransactions(request: Request): TransferTransactions

    data class TransferTransactions(val withdraw: Transaction, val deposit: Transaction)

}

class TransactionCreatorImpl(private val repository: TransactionRepository) : TransactionCreator {

    private val nextId = AtomicLong()

    override fun createTransferTransactions(request: TransactionCreator.Request): TransferTransactions {
        with(request) {
            val fromId = nextId.getAndIncrement()
            val toId = nextId.getAndIncrement()

            val withdraw = Transaction(id = fromId, mirrorTransactionId = toId, money = -money, account = from)
            val deposit = Transaction(id = toId, mirrorTransactionId = fromId, money = money, account = to)

            repository.add(withdraw)
            repository.add(deposit)

            return TransferTransactions(withdraw = withdraw, deposit = deposit)
        }
    }
}