package money.transactions

import clients.Client
import money.Money

//TODO there shouldn't be an id here
data class Transaction(
    val id: Long,
    val mirrorTransactionId: Long,
    val money: Money,
    val from: Client,
    val to: Client
)