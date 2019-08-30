package utils

import java.math.BigDecimal
import java.math.BigDecimal.ZERO

fun <T> Iterable<T>.sumBy(selector: (T) -> BigDecimal): BigDecimal {
    var sum = ZERO
    forEach {
        sum += selector(it)
    }
    return sum
}