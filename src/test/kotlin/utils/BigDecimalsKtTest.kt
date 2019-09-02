package utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class BigDecimalsKtTest {

    @Test
    fun `should give expected sum when not empty`() {
        val list = listOf(100, 300).map { it.toBigDecimal() }
        assertEquals(400.toBigDecimal(), list.sumBy { it })
    }

    @Test
    fun `should give expected sum for single element`() {
        val list = listOf(100.toBigDecimal())
        assertEquals(100.toBigDecimal(), list.sumBy { it })
    }

    @Test
    fun `should give zero when empty`() {
        val list = listOf<BigDecimal>()
        assertEquals(0.toBigDecimal(), list.sumBy { it })
    }
}