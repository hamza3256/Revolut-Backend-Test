package transfer

import money.Money

data class TransferRequest(
    val fromClientId: Long,
    val toClientId: Long,
    val money: Money
)
