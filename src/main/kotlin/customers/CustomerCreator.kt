package customers

import logging.error
import logging.info
import java.util.concurrent.atomic.AtomicLong

interface CustomerCreator {

    /**
     * A request to create a new [Customer]. The [Customer] created from this request will have the same name and surname.
     * */
    data class Request(val name: String, val surname: String)

    /**
     * Create a new [Customer] for the given [request].
     *
     * @return the newly created Customer
     * */
    fun create(request: Request): Customer

}

class CustomerCreatorImpl(private val customerRepository: CustomerRepository) : CustomerCreator {

    private val nextId = AtomicLong()

    override fun create(request: CustomerCreator.Request): Customer {
        with(request) {
            info { "Creating customer for request $this" }
            return Customer(
                id = nextId.getAndIncrement(),
                name = name,
                surname = surname
            ).also { customer ->
                //insert into repository
                if (customerRepository.addCustomer(customer).not()) {
                    //this shouldn't ever happen -> we create a new id each time we create a new customer instance
                    error { "Failed to add customer to repository for request=$request, customer=$customer" }
                    throw RuntimeException("Could not create customer=$customer as it already exists in the repository")
                }
            }
        }
    }
}