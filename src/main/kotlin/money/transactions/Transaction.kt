package money.transactions

import clients.Client
import money.Money

data class Transaction internal constructor(
    val id: Long,
    val mirrorTransactionId: Long,
    val money: Money,
    val from: Client,
    val to: Client
)