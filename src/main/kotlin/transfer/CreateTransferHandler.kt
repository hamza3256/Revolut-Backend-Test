package transfer

import BaseHandler
import clients.accounts.AccountRepository
import clients.accounts.AccountState
import clients.accounts.getAccountOrElse
import io.javalin.Javalin
import io.javalin.http.Context
import logging.info
import logging.verbose
import logging.warn
import money.Money
import org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400
import org.eclipse.jetty.http.HttpStatus.OK_200
import transfer.CreateTransfer.PATH
import transfer.CreateTransfer.RequestBody
import transfer.CreateTransfer.ResponseBody
import transfer.CreateTransferHandler.Result.Failed
import transfer.CreateTransferHandler.Result.Success

class CreateTransferHandler(
    private val accountRepository: AccountRepository,
    private val transferer: MoneyTransferer
) : BaseHandler {

    override fun attach(app: Javalin) {
        verbose { "Attaching CreateTransferHandler" }
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
            //check if client ids are valid
            val fromAccount = accountRepository.getAccountOrElse(fromAccountId) {
                warn { "Could not find a fromAccount for id=${fromAccountId}" }
                return Failed("Could not find a valid account for id=${fromAccountId}")
            }
            val toAccount = accountRepository.getAccountOrElse(toAccountId) {
                warn { "Could not find a toAccount for id=${toAccountId}" }
                return Failed("Could not find a valid account for id=${toAccountId}")
            }

            verbose { "Valid transfer request $this, transferring $money from $fromAccount to $toAccount" }
            return when (val transferResult = transferer.transfer(money, from = fromAccount, to = toAccount)) {
                is TransferResult.Success -> {
                    verbose { "Successfully completed transfer for request $this" }
                    val responseBody = ResponseBody(
                        fromAccountState = transferResult.fromAccountState,
                        toAccountState = transferResult.toAccountState
                    )
                    Success(responseBody)
                }
                else -> {
                    info { "Failed to perform transfer for request $this: $transferResult" }
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