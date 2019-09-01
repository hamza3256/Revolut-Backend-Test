package transfer

import BaseHandler
import clients.ClientRepository
import clients.accounts.AccountState
import clients.getClientOrElse
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
    private val clientRepository: ClientRepository,
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
        val transferRequest = requestBody.toTransferRequest()

        with(transferRequest) {
            //check if client ids are valid
            val fromClient = clientRepository.getClientOrElse(fromClientId) {
                warn { "Could not find a fromClient for id=${fromClientId}" }
                return Failed("Could not find a valid client for id=${fromClientId}")
            }
            val toClient = clientRepository.getClientOrElse(toClientId) {
                warn { "Could not find a toClient for id=${toClientId}" }
                return Failed("Could not find a valid client for id=${toClientId}")
            }

            verbose { "Valid transfer request $this, transferring $money from $fromClient to $toClient" }
            return when (val transferResult = transferer.transfer(money, from = fromClient, to = toClient)) {
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

private fun RequestBody.toTransferRequest(): TransferRequest {
    return TransferRequest(
        fromClientId = fromClientId,
        toClientId = toClientId,
        money = money
    )
}

object CreateTransfer {

    const val PATH = "transfers"

    data class RequestBody(
        val fromClientId: Long,
        val toClientId: Long,
        val money: Money
    )

    data class ResponseBody(
        val fromAccountState: AccountState,
        val toAccountState: AccountState
    )
}