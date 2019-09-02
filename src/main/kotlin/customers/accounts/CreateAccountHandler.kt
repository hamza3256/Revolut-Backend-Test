package customers.accounts

import BaseHandler
import customers.CustomerRepository
import customers.accounts.AccountCreator.Result.Created
import customers.accounts.AccountCreator.Result.NegativeMoney
import customers.accounts.CreateAccount.PATH_PARAM_CUSTOMER_ID
import customers.accounts.CreateAccount.RequestBody
import customers.accounts.CreateAccount.ResponseBody
import customers.accounts.CreateAccountHandler.Result.Failed
import customers.accounts.CreateAccountHandler.Result.Success
import customers.getCustomerOrElse
import io.javalin.Javalin
import io.javalin.http.Context
import money.Money
import org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400
import org.eclipse.jetty.http.HttpStatus.OK_200
import org.slf4j.LoggerFactory
import utils.info
import utils.toLong

class CreateAccountHandler(
    private val accountCreator: AccountCreator,
    private val customerRepository: CustomerRepository
) : BaseHandler {

    private val logger = LoggerFactory.getLogger("CreateAccountHandler")

    override fun attach(app: Javalin) {
        app.post("/customers/:$PATH_PARAM_CUSTOMER_ID/accounts", this)
        logger.info { "Attached CreateAccountHandler" }
    }

    override fun handle(ctx: Context) {

        val result = handleWithResult(ctx)
        ctx.status(result.statusCode)

        when (result) {
            is Success -> {
                logger.info { "CreateAccount processed successfully" }
                ctx.json(result.responseBody)
            }
            is Failed -> {
                logger.info { "Failed to create account" }
                ctx.result(result.message)
            }
        }
    }

    private fun handleWithResult(ctx: Context): Result {
        val customerIdStr = ctx.pathParam(PATH_PARAM_CUSTOMER_ID)
        val customerId = customerIdStr.toLong {
            return Failed("$PATH_PARAM_CUSTOMER_ID isn't a valid Long")
        }

        val requestBody = ctx.body<RequestBody>()
        val startingMoney = requestBody.startingMoney

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

    const val PATH_PARAM_CUSTOMER_ID = "customerId"

    data class RequestBody(val startingMoney: Money)
    data class ResponseBody(val account: Account)

}