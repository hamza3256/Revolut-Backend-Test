package money.exceptions

import money.Money

class InsufficientFundsException(val required: Money, val available: Money) : MoneyException()