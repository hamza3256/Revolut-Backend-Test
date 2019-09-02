package customers.accounts

import customers.Customer
import money.Money
import customers.accounts.AccountCreator.Request
import customers.accounts.AccountCreator.Result
import customers.accounts.AccountCreator.Result.Created
import customers.accounts.AccountCreator.Result.NegativeMoney
import java.util.concurrent.atomic.AtomicLong

interface AccountCreator {

    data class Request(val startingMoney: Money)

    /**
     * Creates and persist a new [Account] for the given [customer] and [request]
     *
     * @return [Created] when a new [Account] was created
     * @return [NegativeMoney] when requested to create an account with a negative starting balance.
     * */
    fun create(customer: Customer, request: Request): Result

    sealed class Result {

        data class Created(val account: Account) : Result()
        object NegativeMoney: Result()
    }
}

class AccountCreatorImpl(private val accountRepository: AccountRepository) : AccountCreator {

    private val nextId = AtomicLong()

    override fun create(customer: Customer, request: Request): Result {
        with(request) {
            if (startingMoney.isNegative()) {
                return NegativeMoney
            }

            return Account(
                id = nextId.getAndIncrement(),
                startingMoney = request.startingMoney,
                customer = customer
            ).let { account ->
                if (accountRepository.addAccount(account)) {
                    //added
                    Created(account)
                } else {
                    //account with such an ID already exists
                    //should never get to this point since we're always incrementing the id
                    throw RuntimeException("Failed to insert $account into repository")
                }
            }
        }
    }
}