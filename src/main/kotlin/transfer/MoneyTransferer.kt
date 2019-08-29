package transfer

import clients.Client
import logging.verbose
import money.Money
import money.account.AccountRepository
import money.transactions.TransactionCreator
import transfer.TransferResult.*

interface MoneyTransferer {

    /**
     * Transfers a positive amount of [money] from client [from] to another clnt [to].
     * Attempting to transfer money between the same clients returns a [TransferResult.Failed].
     * If the source client doesn't have enough money, then it will return [TransferResult.Failed] with [TransferResult.Failed.Cause.INSUFFICIENT_FUNDS] in [TransferResult.Failed.cause]
     *
     * */
    fun transferMoney(
        money: Money,
        from: Client,
        to: Client
    ): TransferResult

}

sealed class TransferResult {

    object Success : TransferResult()
    object InsufficientFunds: TransferResult()
    object NegativeMoney: TransferResult()

}

class MoneyTransfererImpl(
    private val accountRepository: AccountRepository,
    private val transactionCreator: TransactionCreator
) : MoneyTransferer {

    override fun transferMoney(money: Money, from: Client, to: Client): TransferResult {
        verbose { "Money transfer requested: money=$money, from=$from, to=$to" }

        if (money.isNegative()) {
            verbose { "Cannot transfer negative money=$money" }
            return NegativeMoney
        }

        if (money.isZero()) {
            verbose { "Requested to transfer no money, ignoring request" }
            return Success
        }

        val fromAccount = accountRepository.getAccount(from, money.currency)
        if (fromAccount == null) {
            verbose { "$from doesn't have an account for currency=${money.currency}" }
            return InsufficientFunds
        }

        return if (fromAccount hasFunds money) {
            verbose { "$from has sufficient funds to send $money. Transferring to $to" }

            val request = TransactionCreator.Request(money, from = from, to = to)
            transactionCreator.createTransferTransactions(request)
            Success
        } else {
            verbose { "$from has insufficient funds to transfer $money to $to" }
            InsufficientFunds
        }
    }
}