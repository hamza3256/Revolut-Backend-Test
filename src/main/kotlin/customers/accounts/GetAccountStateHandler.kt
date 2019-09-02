package customers.accounts

import BaseHandler
import customers.accounts.GetAccount.PATH_PARAM_ACCOUNT_ID
import customers.accounts.GetAccount.ResponseBody
import customers.accounts.GetAccountStateHandler.Result.Failed
import customers.accounts.GetAccountStateHandler.Result.Success
import io.javalin.Javalin
import io.javalin.http.Context
import org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400
import org.eclipse.jetty.http.HttpStatus.OK_200
import org.slf4j.LoggerFactory
import utils.info
import utils.toLong

class GetAccountStateHandler(
    private val accountRepository: AccountRepository,
    private val accountStateQuerier: AccountStateQuerier
) : BaseHandler {

    private val logger = LoggerFactory.getLogger("GetAccountStateHandler")

    override fun attach(app: Javalin) {
        logger.info { "Attaching CreateTransferHandler" }
        app.get("/accounts/:$PATH_PARAM_ACCOUNT_ID", this)
    }

    override fun handle(ctx: Context) {
        val result = handleWithResult(ctx)
        with(result) {
            ctx.status(responseCode)

            when (this) {
                is Success -> ctx.json(responseBody)
                is Failed -> ctx.result(message)
            }
        }
    }

    private fun handleWithResult(ctx: Context): Result {
        val accountIdStr = ctx.pathParam(PATH_PARAM_ACCOUNT_ID)
        val accountId = accountIdStr.toLong {
            logger.info { "$PATH_PARAM_ACCOUNT_ID path param `$accountIdStr` is not a valid long" }
            return Failed("accountId=`$accountIdStr` is not a valid Long")
        }

        val account = accountRepository.getAccountOrElse(accountId) {
            logger.info { "Could not find account for id=$accountId" }
            return Failed("Account with id=$accountId does not exist")
        }

        val accountState = accountStateQuerier.getCurrentState(account)
        return Success(ResponseBody(accountState))
    }

    sealed class Result(val responseCode: Int) {
        data class Success(val responseBody: ResponseBody) : Result(OK_200)
        data class Failed(val message: String) : Result(BAD_REQUEST_400)
    }
}

object GetAccount {

    const val PATH_PARAM_ACCOUNT_ID = "accountId"

    data class ResponseBody(val accountState: AccountState)

}