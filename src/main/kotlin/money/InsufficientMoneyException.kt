package money

class InsufficientMoneyException(val required: Money, val available: Money) : MoneyException(
    "Insufficient money, required $required but have $available"
)