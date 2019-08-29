import money.Money
import java.util.*

object Currencies {
    val USD = Currency.getInstance(Locale.US)!!
    val GBP = Currency.getInstance(Locale.UK)!!
}

private fun Int.toMoney(currency: Currency): Money {
    return Money(this.toBigDecimal(), currency)
}

val Int.USD: Money
    get() = toMoney(Currencies.USD)

val Int.GBP: Money
    get() = toMoney(Currencies.GBP)