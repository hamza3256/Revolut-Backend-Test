package customers

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InMemoryCustomerRepositoryTest {

    private lateinit var repository: CustomerRepository

    @Before
    fun beforeEachTest() {
        repository = InMemoryCustomerRepository()
    }

    @Test
    fun `adding a Customer should imply that you can get it`() {
        val customer = Customers.nikolay(id = 0)

        //shouldn't exist yet
        assertNull(repository.getCustomer(0))

        assertTrue(repository.addCustomer(customer))

        assertEquals(customer, repository.getCustomer(0))
    }

    @Test
    fun `adding a Customer with the same id should return false`() {
        val customer = Customers.nikolay(id = 0)
        val customerWithSameId = Customers.vlad(id = 0)

        assertTrue(repository.addCustomer(customer))
        assertFalse(repository.addCustomer(customerWithSameId))
    }

    @Test
    fun `adding a Customer with the same id shouldn't replace the original Customer`(){
        val customer = Customers.nikolay(id = 0)
        assertTrue(repository.addCustomer(customer))

        val customerWithSameId = Customers.vlad(id = 0)
        assertFalse(repository.addCustomer(customerWithSameId))

        //shouldn't have been replaced
        assertEquals(customer, repository.getCustomer(0))
    }

    @Test
    fun `getting a Customer for an invalid id should return null`() {
        assertNull(repository.getCustomer(0))
    }

    @Test
    fun `deleteAll() deletes all Customers`(){
        repository.addCustomer(Customers.vlad(0))
        assertNotNull(repository.getCustomer(0))

        repository.deleteAll()
        assertNull(repository.getCustomer(0))
    }
}