package customers.accounts

import customers.accounts.transactions.TransactionRepository
import money.CurrencyMismatchException
import money.Money
import money.NegativeMoneyException
import utils.sumBy

/**
 * Represents the state of an account
 * [money] represents the current amount of money in [account]
 * */
data class AccountState(val account: Account, val money: Money) {

    /**
     * Check whether the AccountState has at least [money]
     * @throws NegativeMoneyException if [money] is negative
     * @throws CurrencyMismatchException if the currency in [money] doesn't match the currency in this AccountState
     * */
    infix fun hasFunds(money: Money): Boolean {
        if (money.isNegative()) {
            throw NegativeMoneyException(money)
        }

        if (this.money.currency != money.currency) {
            throw CurrencyMismatchException(
                expected = this.money.currency,
                actual = money.currency
            )
        }

        return this.money.amount >= money.amount
    }

}

/**
 * Used to query the current state of an account.
 * */
interface AccountStateQuerier {

    /**
     * Return the current state of the given [account], including how much money there is.
     * */
    fun getCurrentState(account: Account): AccountState

}

class AccountStateQuerierImpl(private val transactionRepository: TransactionRepository) : AccountStateQuerier {

    override fun getCurrentState(account: Account): AccountState {
        return AccountState(
            account,
            Money(
                currency = account.currency,
                amount = transactionRepository.getAll(account).sumBy { it.money.amount }
            )
        )
    }
}