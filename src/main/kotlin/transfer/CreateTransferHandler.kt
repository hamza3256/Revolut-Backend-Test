package transfer

import BaseHandler
import customers.accounts.AccountRepository
import customers.accounts.AccountState
import customers.accounts.getAccountOrElse
import io.javalin.Javalin
import io.javalin.http.Context
import money.Money
import org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400
import org.eclipse.jetty.http.HttpStatus.OK_200
import org.slf4j.LoggerFactory
import transfer.CreateTransfer.PATH
import transfer.CreateTransfer.RequestBody
import transfer.CreateTransfer.ResponseBody
import transfer.CreateTransferHandler.Result.Failed
import transfer.CreateTransferHandler.Result.Success
import utils.info
import utils.warn

class CreateTransferHandler(
    private val accountRepository: AccountRepository,
    private val transferer: MoneyTransferer
) : BaseHandler {

    private val logger = LoggerFactory.getLogger("CreateTransferHandler")

    override fun attach(app: Javalin) {
        logger.info { "Attaching CreateTransferHandler" }
        app.post(PATH, this)
    }

    override fun handle(ctx: Context) {
        val result = handleWithResult(ctx)

        with(ctx) {
            status(result.statusCode)

            when (result) {
                is Success -> json(result.responseBody)
                is Failed -> result(result.message)
            }
        }
    }

    sealed class Result(val statusCode: Int) {
        data class Success(val responseBody: ResponseBody) : Result(OK_200)
        data class Failed(val message: String) : Result(BAD_REQUEST_400)
    }

    private fun handleWithResult(ctx: Context): Result {
        val requestBody = ctx.body<RequestBody>()

        with(requestBody) {
            //check if customer ids are valid
            val fromAccount = accountRepository.getAccountOrElse(fromAccountId) {
                logger.warn { "Could not find a fromAccount for id=${fromAccountId}" }
                return Failed("Could not find a valid account for id=${fromAccountId}")
            }
            val toAccount = accountRepository.getAccountOrElse(toAccountId) {
                logger.warn { "Could not find a toAccount for id=${toAccountId}" }
                return Failed("Could not find a valid account for id=${toAccountId}")
            }

            logger.info { "Valid transfer request $this, transferring $money from $fromAccount to $toAccount" }
            return when (val transferResult = transferer.transfer(money, from = fromAccount, to = toAccount)) {
                is TransferResult.Success -> {
                    logger.info { "Successfully completed transfer for request $this" }
                    val responseBody = ResponseBody(
                        fromAccountState = transferResult.fromAccountState,
                        toAccountState = transferResult.toAccountState
                    )
                    Success(responseBody)
                }
                else -> {
                    logger.info { "Failed to perform transfer for request $this: $transferResult" }
                    Failed("Failed to perform transfer: $transferResult")
                }
            }
        }
    }
}

object CreateTransfer {

    const val PATH = "transfers"

    data class RequestBody(
        val fromAccountId: Long,
        val toAccountId: Long,
        val money: Money
    )

    data class ResponseBody(
        val fromAccountState: AccountState,
        val toAccountState: AccountState
    )
}