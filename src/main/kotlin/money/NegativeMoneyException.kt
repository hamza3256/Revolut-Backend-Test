package money

class NegativeMoneyException(money: Money) : MoneyException("Money must not be negative: ${money.amount.toPlainString()}")