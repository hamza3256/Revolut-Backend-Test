package clients.accounts

import clients.Client
import money.Money
import clients.accounts.AccountCreator.Request
import clients.accounts.AccountCreator.Result
import clients.accounts.AccountCreator.Result.Created
import clients.accounts.AccountCreator.Result.Failed
import clients.accounts.AccountCreator.Result.Failed.Cause.ALREADY_EXISTS
import clients.accounts.AccountCreator.Result.Failed.Cause.NEGATIVE_MONEY
import java.util.concurrent.atomic.AtomicLong

interface AccountCreator {

    data class Request(val startingMoney: Money)

    fun create(client: Client, request: Request): Result

    sealed class Result {

        data class Created(val account: Account) : Result()

        data class Failed(val cause: Cause) : Result() {

            enum class Cause {
                ALREADY_EXISTS,
                NEGATIVE_MONEY
            }
        }
    }
}

class AccountCreatorImpl(private val accountRepository: AccountRepository) : AccountCreator {

    private val nextId = AtomicLong()

    override fun create(client: Client, request: Request): Result {
        with(request) {
            if (startingMoney.isNegative()) {
                return Failed(NEGATIVE_MONEY)
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
                    //client already has an account for the given currency
                    Failed(ALREADY_EXISTS)
                }
            }
        }
    }
}