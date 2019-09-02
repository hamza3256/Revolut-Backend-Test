package customers.accounts.transactions

import customers.accounts.Account
import money.CurrencyMismatchException
import money.Money

/**
 * Represents a monetary transaction in the account.
 *
 * [mirrorTransactionId] is the id of another transaction which it is related to.
 * For example, transferring money between accounts leads to 2 Transactions, one of which is a withdrawal and the other is a deposit.
 * Each transaction has a unique id, and the [mirrorTransactionId] then acts as a reference to the other transaction.
 * The [mirrorTransactionId] can be used to determine the source/target of a transfer in funds
 *
 * [account] the account to which the transaction belongs t
 * [money] the change in money, negative a transfer out of the account, positive means it was deposited into the account
 * */
data class Transaction internal constructor(
    val id: Long,
    val mirrorTransactionId: Long? = null,
    val account: Account,
    val money: Money
) {

    init {
        if (account.currency != money.currency) {
            throw CurrencyMismatchException(expected = account.currency, actual = money.currency)
        }
    }
}