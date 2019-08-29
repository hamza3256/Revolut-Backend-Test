package money.account

import clients.Client
import logging.verbose
import money.Money
import money.account.AccountCreator.Request
import money.account.AccountCreator.Result
import money.account.AccountCreator.Result.*
import money.account.AccountCreator.Result.Failed.Cause.ALREADY_EXISTS
import money.account.AccountCreator.Result.Failed.Cause.NEGATIVE_MONEY
import java.util.concurrent.atomic.AtomicLong

interface AccountCreator {

    data class Request(val money: Money, val token: String)

    fun create(client: Client, request: Request): Result

    sealed class Result {

        object Created : Result()
        object AlreadyCreated : Result()
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
    private val handled = mutableMapOf<String, HandledCreateAccountRequest>()

    override fun create(client: Client, request: Request): Result {
        with(request) {
            if (token in handled) {
                //already handled
                verbose { "Requested to create an Account for $client with request=$request but we've already created it" }
                return AlreadyCreated
            }

            if (money.isNegative()) {
                return Failed(NEGATIVE_MONEY)
            }

            return Account(
                id = nextId.getAndIncrement(),
                currency = money.currency,
                transactions = emptyList(),
                startingMoney = money.amount
            ).let { account ->
                if (accountRepository.addAccount(client, account)) {
                    //added
                    Created
                } else {
                    //client already has an account for the given currency
                    Failed(ALREADY_EXISTS)
                }
            }
        }
    }

    private data class HandledCreateAccountRequest(
        private val client: Client,
        private val request: Request,
        private val result: Result
    )
}