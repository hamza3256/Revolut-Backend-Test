package money.transactions

import money.Money
import money.account.Account

data class Transaction internal constructor(
    val id: Long,
    val mirrorTransactionId: Long,
    val account: Account,
    val money: Money
)