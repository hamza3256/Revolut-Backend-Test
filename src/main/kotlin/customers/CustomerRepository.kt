package customers

import logging.info

interface CustomerRepository {

    /**
     * Adds a [customer] to the repository if a customer with the given id doesn't exist in the repository
     * @return true if the customer didn't exist and was added, false if it already exists.
     */
    fun addCustomer(customer: Customer): Boolean

    /**
     * Gets a [Customer] from the repository for the given [id]. If a customer with [id] does not exist, it returns null
     * @return a [Customer] with the given [id] or null if there is no such [Customer]
     * */
    fun getCustomer(id: Long): Customer?

    /**
     * Delete all Customers, but does not delete corresponding Accounts or Transactions
     * */
    fun deleteAll()

}

/**
 * A [CustomerRepository] which stores [Customer] in memory.
 * Project requirements state to use an in-memory datastore. Usually it would be persisted into an SQL database or similar
 * */
class InMemoryCustomerRepository : CustomerRepository {
    private val idsToCustomers = mutableMapOf<Long, Customer>()

    override fun addCustomer(customer: Customer): Boolean {
        return if (customer.id in idsToCustomers) {
            info { "$customer is already present in the repository" }
            false
        } else {
            idsToCustomers[customer.id] = customer
            info { "$customer added to the repository" }
            true
        }
    }

    override fun getCustomer(id: Long): Customer? = idsToCustomers[id]

    override fun deleteAll() = idsToCustomers.clear()
}

inline fun CustomerRepository.getCustomerOrElse(id: Long, whenNoSuchCustomer: (Long) -> Customer): Customer {
    return this.getCustomer(id) ?: whenNoSuchCustomer(id)
}