package customers.accounts

import customers.Customer
import money.Money
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.util.*

data class Account(
    val id: Long,
    val customer: Customer,
    val startingMoney: Money
) {

    val currency = startingMoney.currency

    constructor(id: Long, customer: Customer, currency: Currency, startingMoney: BigDecimal = ZERO) : this(
        id = id,
        customer = customer,
        startingMoney = Money(
            currency = currency,
            amount = startingMoney
        )
    )
}