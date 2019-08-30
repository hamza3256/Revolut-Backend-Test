package money

import java.util.*

class CurrencyMismatchException(expected: Currency, actual: Currency) :
    Throwable("Expected currency ${expected.currencyCode} but got ${actual.currencyCode}")