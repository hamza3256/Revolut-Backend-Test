package money.transactions

import money.Money
import clients.accounts.Account

data class Transaction internal constructor(
    val id: Long,
    val mirrorTransactionId: Long,
    val account: Account,
    val money: Money
)