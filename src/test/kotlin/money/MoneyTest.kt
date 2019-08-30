package money

import Currencies.USD
import USD
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal.ZERO

class MoneyTest {

    @Test
    fun `test additive inverse property`() {
        //additive inverse property: a + (-a) = 0
        val ten = 10.USD
        val minusTen = -ten

        assertEquals(0.toBigDecimal(), ten.amount + minusTen.amount)
    }

    @Test
    fun `test double negation cancels negations`() {
        //-(-a) = a
        val ten = 10.USD
        val minusMinusTen = -(-ten)
        assertEquals(ten, minusMinusTen)
    }

    @Test
    fun `test zero negated is still zero`() {
        //0USD == 0USD and not -0USD
        assertEquals(ZERO, (-(0.USD)).amount)
    }

    @Test
    fun `test negation preserves currency`() {
        assertEquals(USD, (-(10.USD)).currency)
    }

    @Test
    fun `test isNegative`() {
        val negativeMoney = (-10).USD
        assertTrue(negativeMoney.isNegative())

        val zeroMoney = 0.USD
        assertFalse(zeroMoney.isNegative())

        val minusZeroMoney = (-0).USD
        assertFalse(minusZeroMoney.isNegative())

        val positiveMoney = 10.USD
        assertFalse(positiveMoney.isNegative())
    }

    @Test
    fun `test isPositive`() {
        val positiveMoney = 10.USD
        assertTrue(positiveMoney.isPositive())

        val negativeMoney = (-10).USD
        assertFalse(negativeMoney.isPositive())

        val zeroMoney = 0.USD
        assertFalse(zeroMoney.isPositive())

        val minusZeroMoney = (-0).USD
        assertFalse(minusZeroMoney.isPositive())
    }

    @Test
    fun `test isZero`() {
        val zeroMoney = 0.USD
        assertTrue(zeroMoney.isZero())

        val minusZeroMoney = (-0).USD
        assertTrue(minusZeroMoney.isZero())

        val positiveMoney = 10.USD
        assertFalse(positiveMoney.isZero())

        val negativeMoney = (-10).USD
        assertFalse(negativeMoney.isZero())
    }

    @Test
    fun `test negation creates new object`() {
        val original = 0.USD
        val negated = -original

        assertNotSame(original, negated)
    }
}