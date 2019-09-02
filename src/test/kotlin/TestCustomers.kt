import customers.Customer

object Customers {

    /**
     * Creates a new Customer representing Vlad Yatsenko with id 0
     * */
    fun vlad(id: Long = 0): Customer {
        return Customer(id, "Vlad", "Yatsenko")
    }

    /**
     * Creates a new Customer representing Nikolay Storonsky with id 0
     * */
    fun nikolay(id: Long = 0): Customer {
        return Customer(id, "Nikolay", "Storonsky")
    }

}