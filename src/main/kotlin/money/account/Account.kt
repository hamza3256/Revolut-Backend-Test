package money.account

import money.Money
import money.exceptions.CurrencyMismatchException
import money.exceptions.NegativeMoneyException
import money.transactions.Transaction
import utils.sumBy
import java.math.BigDecimal
import java.util.*

data class Account(
    val id: Long,
    val currency: Currency,
    val transactions: List<Transaction>,
    val startingMoney: BigDecimal
) {

    private val money = Money(
        amount = startingMoney + transactions.sumBy { it.money.amount },
        currency = currency
    )

    //TODO handle NegativeMoneyException
    infix fun hasFunds(money: Money): Boolean {
        if (money.isNegative()) {
            throw NegativeMoneyException(money)
        }

        if (this.currency != money.currency) {
            throw CurrencyMismatchException(
                expected = this.currency,
                actual = money.currency
            )
        }

        if (money.isZero()) return true

        return this.money.amount >= money.amount
    }
}