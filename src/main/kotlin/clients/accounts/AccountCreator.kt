package clients.accounts

import clients.Client
import money.Money
import clients.accounts.AccountCreator.Request
import clients.accounts.AccountCreator.Result
import clients.accounts.AccountCreator.Result.Created
import clients.accounts.AccountCreator.Result.NegativeMoney
import java.util.concurrent.atomic.AtomicLong

interface AccountCreator {

    data class Request(val startingMoney: Money)

    /**
     * Creates and persist a new [Account] for the given [client] and [request]
     *
     * @return [Created] when a new [Account] was created
     * @return [NegativeMoney] when requested to create an account with a starting negative balance.
     * */
    fun create(client: Client, request: Request): Result

    sealed class Result {

        data class Created(val account: Account) : Result()
        object NegativeMoney: Result()
    }
}

class AccountCreatorImpl(private val accountRepository: AccountRepository) : AccountCreator {

    private val nextId = AtomicLong()

    override fun create(client: Client, request: Request): Result {
        with(request) {
            if (startingMoney.isNegative()) {
                return NegativeMoney
            }

            return Account(
                id = nextId.getAndIncrement(),
                startingMoney = request.startingMoney,
                client = client
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