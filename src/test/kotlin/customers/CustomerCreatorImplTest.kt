package customers

import org.junit.Assert.assertEquals
import org.junit.Test

class CustomerCreatorImplTest {

    private val customerRepository = InMemoryCustomerRepository()
    private val customerCreator = CustomerCreatorImpl(customerRepository)
    private val nikolayRequest = CustomerCreator.Request(name = "Nikolay", surname = "Storonsky")

    @Test
    fun `creating a Customer with the same request should create a new Customer with a new id`() {
        val customers = (0..1).map { customerCreator.create(nikolayRequest) }
        val distinctById = customers.distinctBy { it.id }

        assertEquals(customers, distinctById)
    }

    @Test
    fun `created Customer should have the same name and surname as in the request`() {
        val customer = customerCreator.create(nikolayRequest)

        assertEquals(nikolayRequest.name, customer.name)
        assertEquals(nikolayRequest.surname, customer.surname)
    }

    @Test
    fun `created Customer should be inserted into the repository`() {
        val customer = customerCreator.create(nikolayRequest)
        val customerFromRepo = customerRepository.getCustomer(customer.id)

        assertEquals(customer, customerFromRepo)
    }
}