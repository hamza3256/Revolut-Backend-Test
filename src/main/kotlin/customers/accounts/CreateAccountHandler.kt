package customers.accounts

import BaseHandler
import customers.CustomerRepository
import customers.accounts.AccountCreator.Result.Created
import customers.accounts.AccountCreator.Result.NegativeMoney
import customers.accounts.CreateAccount.PATH
import customers.accounts.CreateAccount.RequestBody
import customers.accounts.CreateAccount.ResponseBody
import customers.accounts.CreateAccountHandler.Result.Failed
import customers.accounts.CreateAccountHandler.Result.Success
import customers.getCustomerOrElse
import io.javalin.Javalin
import io.javalin.http.Context
import logging.info
import logging.verbose
import money.Money
import org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400
import org.eclipse.jetty.http.HttpStatus.OK_200

class CreateAccountHandler(
    private val accountCreator: AccountCreator,
    private val customerRepository: CustomerRepository
) : BaseHandler {

    override fun attach(app: Javalin) {
        app.post(PATH, this)
        info { "Attached CreateAccountHandler" }
    }

    override fun handle(ctx: Context) {

        val result = handleWithResult(ctx)
        ctx.status(result.statusCode)

        when (result) {
            is Success -> {
                verbose { "CreateAccount processed successfully" }
                ctx.json(result.responseBody)
            }
            is Failed -> {
                verbose { "Failed to create account" }
                ctx.result(result.message)
            }
        }
    }

    private fun handleWithResult(ctx: Context): Result {
        val (customerId, startingMoney) = ctx.body<RequestBody>()

        val customer = customerRepository.getCustomerOrElse(customerId) { id ->
            return Failed("Customer not found for id=$id")
        }

        val accountCreatorRequest = AccountCreator.Request(startingMoney = startingMoney)
        return when (val result = accountCreator.create(customer, accountCreatorRequest)) {
            is Created -> Success(ResponseBody(result.account))
            is NegativeMoney -> Failed("Account must start with a non-negative balance")
        }
    }

    sealed class Result(val statusCode: Int) {

        data class Success(val responseBody: ResponseBody) : Result(OK_200)
        data class Failed(val message: String) : Result(BAD_REQUEST_400)
    }
}

object CreateAccount {

    const val PATH = "accounts"

    data class RequestBody(val customerId: Long, val startingMoney: Money)
    data class ResponseBody(val account: Account)

}