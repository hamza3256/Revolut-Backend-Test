package money

import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.util.*

data class Money(val amount: BigDecimal, val currency: Currency) {

    fun isNegative(): Boolean = amount < ZERO
    fun isPositive(): Boolean = amount > ZERO
    fun isZero(): Boolean = amount == ZERO

    operator fun unaryMinus(): Money {
        return copy(amount = -amount)
    }
}