package customers.accounts

import customers.Customer
import customers.accounts.AccountCreator.Request
import java.util.*
import java.util.concurrent.atomic.AtomicLong

interface AccountCreator {

    data class Request(val currency: Currency)

    /**
     * Creates and persist a new [Account] for the given [customer] and [request]
     *
     * @return the created [Account]
     * */
    fun create(customer: Customer, request: Request): Account

}

class AccountCreatorImpl(private val accountRepository: AccountRepository) : AccountCreator {

    private val nextId = AtomicLong()

    override fun create(customer: Customer, request: Request): Account {
        with(request) {

            return Account(
                id = nextId.getAndIncrement(),
                customer = customer,
                currency = currency
            ).also { account ->
                //add to repo
                if (!accountRepository.addAccount(account)) {
                    //account with such an ID already exists
                    //should never get to this point since we're always incrementing the id
                    throw RuntimeException("Failed to insert $account into repository")
                }
            }
        }
    }
}