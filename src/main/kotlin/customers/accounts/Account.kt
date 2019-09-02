package customers.accounts

import customers.Customer
import java.util.*

data class Account(
    val id: Long,
    val customer: Customer,
    val currency: Currency
)