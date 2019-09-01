package transfer

import clients.Client
import logging.verbose
import money.Money
import clients.accounts.AccountRepository
import clients.accounts.AccountState
import clients.accounts.AccountStateQuerier
import clients.accounts.getAccountOrElse
import clients.transactions.TransactionCreator
import transfer.TransferResult.*

interface MoneyTransferer {

    /**
     * Transfers a positive amount of [money] from client [from] to another clnt [to].
     * Attempting to transfer money between the same clients will be ignored and a [SameAccount] will be returned.
     * If the source client doesn't have enough money, then it will return [TransferResult.Failed] with [TransferResult.Failed.Cause.INSUFFICIENT_FUNDS] in [TransferResult.Failed.cause]
     * If either the recipient or the sender do not have an account, then a [MissingAccount] result is returned
     *
     * */
    fun transfer(
        money: Money,
        from: Client,
        to: Client
    ): TransferResult

}

sealed class TransferResult {

    //TODO test returned fields have expected values
    data class Success(val fromAccountState: AccountState, val toAccountState: AccountState) : TransferResult()
    object SameAccount : TransferResult()
    object InsufficientFunds : TransferResult()
    data class MissingAccount(val client: Client) : TransferResult()
    object NegativeMoney : TransferResult()

}

class MoneyTransfererImpl(
    private val accountRepository: AccountRepository,
    private val transactionCreator: TransactionCreator,
    private val accountStateQuerier: AccountStateQuerier
) : MoneyTransferer {

    override fun transfer(money: Money, from: Client, to: Client): TransferResult {
        verbose { "Money transfer requested: money=$money, from=$from, to=$to" }

        if (from.id == to.id) {
            verbose { "Requested transfer between same account $from" }
            return SameAccount
        }

        if (money.isNegative()) {
            verbose { "Cannot transfer negative money=$money" }
            return NegativeMoney
        }

        val currency = money.currency
        val fromAccount = accountRepository.getAccountOrElse(from, currency) {
            verbose { "$from doesn't have an account for currency=$currency" }
            return MissingAccount(from)
        }

        //check if recipient has an account with the given currency
        val toAccount = accountRepository.getAccountOrElse(to, currency) {
            verbose { "$to doesn't have an account to accept transfer from currency=$currency" }
            return MissingAccount(to)
        }

        var fromAccountState = accountStateQuerier.getCurrentState(fromAccount)

        if (money.isZero()) {
            verbose { "Requested to transfer no money, ignoring request" }
            val toAccountState = accountStateQuerier.getCurrentState(toAccount)
            return Success(fromAccountState = fromAccountState, toAccountState = toAccountState)
        }

        return if (fromAccountState hasFunds money) {
            verbose { "$from has sufficient funds to send $money. Transferring to $to" }

            val request = TransactionCreator.Request(money, from = fromAccount, to = toAccount)
            transactionCreator.createTransferTransactions(request)

            fromAccountState = accountStateQuerier.getCurrentState(fromAccount)
            val toAccountState = accountStateQuerier.getCurrentState(toAccount)
            Success(fromAccountState= fromAccountState, toAccountState = toAccountState)
        } else {
            verbose { "$from has insufficient funds to transfer $money to $to" }
            InsufficientFunds
        }
    }
}