package transfer

import clients.accounts.*
import clients.transactions.TransactionCreator
import logging.verbose
import money.Money
import transfer.TransferResult.*

interface MoneyTransferer {

    /**
     * Transfers a positive amount of [money] from account [from] to another account [to].
     * If [money] is zero, then [Success] is immediately returned, but not transactions occur.
     * Attempting to transfer money between the same accounts will be ignored and a [SameAccount] result will be returned.
     * If the source account doesn't have enough money, then it will return [TransferResult.InsufficientFunds]
     * If [money] is negative, then [NegativeMoney] is returned
     * If either of the Accounts have the a different currency than in [money], then a [CurrencyMismatch] result is returned
     *
     * */
    fun transfer(
        money: Money,
        from: Account,
        to: Account
    ): TransferResult

}

sealed class TransferResult {

    data class Success(val fromAccountState: AccountState, val toAccountState: AccountState) : TransferResult()
    object SameAccount : TransferResult()
    object InsufficientFunds : TransferResult()
    object NegativeMoney : TransferResult()
    object CurrencyMismatch: TransferResult()

}

class MoneyTransfererImpl(
    private val transactionCreator: TransactionCreator,
    private val accountStateQuerier: AccountStateQuerier
) : MoneyTransferer {

    override fun transfer(money: Money, from: Account, to: Account): TransferResult {
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

        //check if from is correct currency
        if(from.currency != currency){
            verbose { "from has incorrect currency. Expected=${currency.currencyCode} but got ${from.currency.currencyCode}" }
            return CurrencyMismatch
        }

        //check if to is correct currency
        if(to.currency != currency){
            verbose { "to has incorrect currency. Expected=${currency.currencyCode} but got ${to.currency.currencyCode}" }
            return CurrencyMismatch
        }

        var fromAccountState = accountStateQuerier.getCurrentState(from)

        if (money.isZero()) {
            verbose { "Requested to transfer no money, ignoring request" }
            val toAccountState = accountStateQuerier.getCurrentState(to)
            return Success(fromAccountState = fromAccountState, toAccountState = toAccountState)
        }

        return if (fromAccountState hasFunds money) {
            verbose { "$from has sufficient funds to send $money. Transferring to $to" }

            val request = TransactionCreator.Request(money, from = from, to = to)
            transactionCreator.createTransferTransactions(request)

            fromAccountState = accountStateQuerier.getCurrentState(from)
            val toAccountState = accountStateQuerier.getCurrentState(to)
            Success(fromAccountState = fromAccountState, toAccountState = toAccountState)
        } else {
            verbose { "$from has insufficient funds to transfer $money to $to" }
            InsufficientFunds
        }
    }
}