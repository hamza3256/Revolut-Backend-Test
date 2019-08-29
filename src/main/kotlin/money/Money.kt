package money

import java.math.BigDecimal
import java.util.*

data class Money(val amount: BigDecimal, val currency: Currency) {

    fun isNegative(): Boolean = amount < BigDecimal.ZERO
    fun isPositive(): Boolean = amount > BigDecimal.ZERO
    fun isZero(): Boolean = amount == BigDecimal.ZERO

    operator fun unaryMinus(): Money {
        return copy(amount = -amount)
    }
}