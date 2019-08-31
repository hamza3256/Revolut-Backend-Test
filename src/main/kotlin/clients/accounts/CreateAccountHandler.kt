package clients.accounts

import BaseHandler
import clients.ClientRepository
import clients.accounts.AccountCreator.Result.Created
import clients.accounts.AccountCreator.Result.Failed
import clients.accounts.AccountCreator.Result.Failed.Cause.ALREADY_EXISTS
import clients.accounts.AccountCreator.Result.Failed.Cause.NEGATIVE_MONEY
import clients.accounts.CreateAccount.PARAM_CLIENT_ID
import clients.accounts.CreateAccount.PATH
import clients.accounts.CreateAccountHandler.Result.*
import clients.getClientOrElse
import io.javalin.Javalin
import io.javalin.http.Context
import logging.debug
import logging.info
import logging.verbose
import org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400
import org.eclipse.jetty.http.HttpStatus.OK_200
import utils.requireParam
import utils.toLong

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
                ctx.json(result.account)
            }
            is MissingParam -> {
                info { "Missing param: $result" }
                ctx.result("Missing param=${result.paramName}")
            }
            is InvalidParam -> {
                info { "Invalid: $result" }
                ctx.result("Invalid param=${result.paramName} with value=${result.paramValue}: ${result.message}")
            }
            is Unsatisfiable -> {
                info { "Unsatisfiable: $result" }
                ctx.result("Request couldn't be satisfied: ${result.message}")
            }
        }
    }

    private fun handleWithResult(ctx: Context): Result {

        debug { "body=${ctx.body()}" }

        val request = ctx.body<AccountCreator.Request>()
        val clientIdParam = ctx.requireParam(PARAM_CLIENT_ID) { param ->
            return MissingParam(param)
        }

        val clientId = clientIdParam.toLong { value ->
            return InvalidParam(paramName = PARAM_CLIENT_ID, paramValue = value, message = "Not a valid Long")
        }

        val client = clientRepository.getClientOrElse(clientId) { id ->
            return InvalidParam(
                paramName = PARAM_CLIENT_ID,
                paramValue = id.toString(),
                message = "Could not find a client with the given id"
            )
        }

        return when (val result = accountCreator.create(client, request)) {
            is Created -> {
                Success(result.account)
            }
            is Failed -> {
                when (result.cause) {
                    ALREADY_EXISTS -> {
                        val currencyCode = request.startingMoney.currency.currencyCode
                        Unsatisfiable("Account with currency $currencyCode already exists")
                    }
                    NEGATIVE_MONEY -> {
                        Unsatisfiable("Account must start with a non-negative balance")
                    }
                }
            }
        }
    }

    sealed class Result(val statusCode: Int) {

        data class Success(val account: Account) : Result(OK_200)
        data class MissingParam(val paramName: String) : Result(BAD_REQUEST_400)

        data class InvalidParam(
            val paramName: String,
            val paramValue: String,
            val message: String
        ) : Result(BAD_REQUEST_400)

        data class Unsatisfiable(val message: String) : Result(BAD_REQUEST_400)

    }
}

private object CreateAccount {

    const val PATH = "accounts"

    const val PARAM_CLIENT_ID = "clientId"

}