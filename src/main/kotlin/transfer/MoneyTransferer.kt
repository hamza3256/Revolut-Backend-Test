package transfer

import customers.accounts.*
import customers.accounts.transactions.Transaction
import customers.accounts.transactions.TransactionCreator
import money.Money
import org.slf4j.LoggerFactory
import transfer.TransferResult.*
import utils.info

interface MoneyTransferer {

    /**
     * Transfers a non-negative amount of [money] from account [from] to another account [to].
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

    data class Success(val fromTransaction: Transaction, val toTransaction: Transaction) : TransferResult()
    object SameAccount : TransferResult()
    object InsufficientFunds : TransferResult()
    object NegativeMoney : TransferResult()
    object CurrencyMismatch: TransferResult()

}

class MoneyTransfererImpl(
    private val transactionCreator: TransactionCreator,
    private val accountStateQuerier: AccountStateQuerier
) : MoneyTransferer {

    private val logger = LoggerFactory.getLogger("MoneyTransfererImpl")

    override fun transfer(money: Money, from: Account, to: Account): TransferResult {
        logger.info { "Money transfer requested: money=$money, from=$from, to=$to" }

        if (from.id == to.id) {
            logger.info { "Requested transfer between same account $from" }
            return SameAccount
        }

        if (money.isNegative()) {
            logger.info { "Cannot transfer negative money=$money" }
            return NegativeMoney
        }

        val currency = money.currency

        //check if from is correct currency
        if(from.currency != currency){
            logger.info { "from has incorrect currency. Expected=${currency.currencyCode} but got ${from.currency.currencyCode}" }
            return CurrencyMismatch
        }

        //check if to is correct currency
        if(to.currency != currency){
            logger.info { "to has incorrect currency. Expected=${currency.currencyCode} but got ${to.currency.currencyCode}" }
            return CurrencyMismatch
        }

        val fromAccountState = accountStateQuerier.getCurrentState(from)

        return if (fromAccountState hasFunds money) {
            logger.info { "$from has sufficient funds to send $money. Transferring to $to" }

            val request = TransactionCreator.Request(money, from = from, to = to)
            val createdTransactions = transactionCreator.createTransferTransactions(request)

            Success(fromTransaction = createdTransactions.fromTransaction, toTransaction = createdTransactions.toTransaction)
        } else {
            logger.info { "$from has insufficient funds to transfer $money to $to" }
            InsufficientFunds
        }
    }
}