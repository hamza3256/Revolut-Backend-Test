package clients.accounts

import BaseHandler
import clients.ClientRepository
import clients.getClientOrElse
import io.javalin.Javalin
import io.javalin.http.Context
import logging.info
import logging.verbose
import logging.warn
import clients.accounts.AccountCreator.Result.Created
import clients.accounts.AccountCreator.Result.Failed
import clients.accounts.AccountCreator.Result.Failed.Cause.ALREADY_EXISTS
import clients.accounts.AccountCreator.Result.Failed.Cause.NEGATIVE_MONEY
import clients.accounts.CreateAccount.PARAM_CLIENT_ID
import clients.accounts.CreateAccount.PATH
import utils.requireParam
import utils.toLong

class AccountHandler(
    private val accountCreator: AccountCreator,
    private val clientRepository: ClientRepository
) : BaseHandler {

    override fun attach(app: Javalin) {
        app.post(PATH, this)
        info { "Attached AccountHandler" }
    }

    override fun handle(ctx: Context) {
        val request = ctx.body<AccountCreator.Request>()
        val clientIdParam = ctx.requireParam(PARAM_CLIENT_ID) { param ->
            warn { "Missing param=$param" }
            return
        }

        val clientId = clientIdParam.toLong {
            warn { "$PARAM_CLIENT_ID=$it isn't a valid long" }
            return
        }

        val client = clientRepository.getClientOrElse(clientId) { id ->
            warn { "Client with id=$id does not exist" }
            return
        }

        when (val result = accountCreator.create(client, request)) {
            is Created -> {
                verbose { "Successfully created ${result.account} for request=$request" }
                ctx.status(200)
            }
            is Failed -> {
                when (result.cause) {
                    ALREADY_EXISTS -> {
                        verbose { "$client already has an account with currency=${request.startingMoney.currency}" }
                        ctx.status(400)
                        ctx.result("Account with given currency already exists")
                    }
                    NEGATIVE_MONEY -> {
                        verbose { "$client cannot start with an account that has a negative balance: request=$request" }
                        ctx.status(400)
                        ctx.result("Account must start with a non-negative balance")
                    }
                }
            }
        }
    }
}

private object CreateAccount {

    const val PATH = "account"

    const val PARAM_CLIENT_ID = "clientId"

}