package clients.accounts

import BaseHandler
import clients.ClientRepository
import clients.accounts.AccountCreator.Result.Created
import clients.accounts.AccountCreator.Result.Failed.Cause.ALREADY_EXISTS
import clients.accounts.AccountCreator.Result.Failed.Cause.NEGATIVE_MONEY
import clients.accounts.CreateAccount.PATH
import clients.accounts.CreateAccount.RequestBody
import clients.accounts.CreateAccount.ResponseBody
import clients.accounts.CreateAccountHandler.Result.*
import clients.getClientOrElse
import io.javalin.Javalin
import io.javalin.http.Context
import logging.info
import logging.verbose
import money.Money
import org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400
import org.eclipse.jetty.http.HttpStatus.OK_200
import clients.accounts.AccountCreator.Result.Failed as CreateAccountFailed

class CreateAccountHandler(
    private val accountCreator: AccountCreator,
    private val clientRepository: ClientRepository
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
        val (clientId, startingMoney) = ctx.body<RequestBody>()

        val client = clientRepository.getClientOrElse(clientId) { id ->
            return Failed("Client not found for id=$id")
        }

        val accountCreatorRequest = AccountCreator.Request(startingMoney = startingMoney)
        return when (val result = accountCreator.create(client, accountCreatorRequest)) {
            is Created -> Success(ResponseBody(result.account))
            is CreateAccountFailed -> {
                when (result.cause) {
                    ALREADY_EXISTS -> {
                        val currencyCode = startingMoney.currency.currencyCode
                        Failed("Account with currency $currencyCode already exists")
                    }
                    NEGATIVE_MONEY -> {
                        Failed("Account must start with a non-negative balance")
                    }
                }
            }
        }
    }

    sealed class Result(val statusCode: Int) {

        data class Success(val responseBody: ResponseBody) : Result(OK_200)
        data class Failed(val message: String) : Result(BAD_REQUEST_400)
    }
}

object CreateAccount {

    const val PATH = "accounts"

    data class RequestBody(val clientId: Long, val startingMoney: Money)
    data class ResponseBody(val account: Account)

}