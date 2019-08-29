package utils

import java.math.BigDecimal
import java.math.BigDecimal.ZERO

fun Iterable<BigDecimal>.sum(): BigDecimal {
    var sum = ZERO
    forEach {
        sum += it
    }
    return sum
}

fun <T> Iterable<T>.sumBy(selector: (T) -> BigDecimal): BigDecimal {
    var sum = ZERO
    forEach {
        sum += selector(it)
    }
    return sum
}