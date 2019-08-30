package money.account

import clients.Client
import money.Money
import money.exceptions.CurrencyMismatchException
import money.exceptions.NegativeMoneyException
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.util.*

data class Account(
    val id: Long,
    val client: Client,
    val startingMoney: Money
) {

    val currency = startingMoney.currency

    //TODO use this constructor where applicable
    constructor(id: Long, client: Client, currency: Currency, startingMoney: BigDecimal = ZERO) : this(
        id = id,
        client = client,
        startingMoney = Money(
            currency = currency,
            amount = startingMoney
        )
    )
}