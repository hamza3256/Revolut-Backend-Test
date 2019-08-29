package transfer

import clients.Client
import logging.verbose
import money.Money
import money.account.AccountRepository
import money.transactions.TransactionCreator
import transfer.TransferResult.Failed
import transfer.TransferResult.Failed.Cause.INSUFFICIENT_FUNDS
import transfer.TransferResult.Failed.Cause.NEGATIVE_MONEY
import transfer.TransferResult.Success

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
    data class Failed(val cause: Cause) : TransferResult() {

        //TODO don' use an enum, instead use another sealed class so we can know much money is still required when using INSUFFICIENT_FUNDS
        enum class Cause {
            INSUFFICIENT_FUNDS,
            NEGATIVE_MONEY
        }
    }
}

class MoneyTransfererImpl(
    private val accountRepository: AccountRepository,
    private val transactionCreator: TransactionCreator
) : MoneyTransferer {

    override fun transferMoney(money: Money, from: Client, to: Client): TransferResult {
        verbose { "Money transfer requested: money=$money, from=$from, to=$to" }

        if (money.isNegative()) {
            verbose { "Cannot transfer negative money=$money" }
            return Failed(NEGATIVE_MONEY)
        }

        if (money.isZero()) {
            verbose { "Requested to transfer no money, ignoring request" }
            return Success
        }

        val fromAccount = accountRepository.getAccount(from, money.currency)
        if (fromAccount == null) {
            verbose { "$from doesn't have an account for currency=${money.currency}" }
            return Failed(INSUFFICIENT_FUNDS)
        }

        return if (fromAccount hasFunds money) {
            verbose { "$from has sufficient funds to send $money. Transferring to $to" }

            val request = TransactionCreator.Request(money, from = from, to = to)
            transactionCreator.createTransferTransactions(request)
            Success
        } else {
            verbose { "$from has insufficient funds to transfer $money to $to" }
            Failed(INSUFFICIENT_FUNDS)
        }
    }
}