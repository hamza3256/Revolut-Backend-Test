package clients.accounts

import money.Money
import money.transactions.TransactionRepository
import money.exceptions.NegativeMoneyException
import  money.exceptions.CurrencyMismatchException
import utils.sumBy

data class AccountState(val account: Account, val money: Money){
    //TODO handle NegativeMoneyException
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

        if (money.isZero()) return true

        return this.money.amount >= money.amount
    }

}

interface AccountStateQuerier {

    fun getCurrentState(account: Account): AccountState

}

class AccountStateQuerierImpl(private val transactionRepository: TransactionRepository) : AccountStateQuerier {

    override fun getCurrentState(account: Account): AccountState {
        return AccountState(
            account,
            Money(
                currency = account.currency,
                amount = account.startingMoney.amount + transactionRepository.getAll(account).sumBy { it.money.amount }
            )
        )
    }
}