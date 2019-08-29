package transfer

import BaseHandler
import clients.ClientRepository
import clients.getClientOrElse
import io.javalin.Javalin
import io.javalin.http.Context
import logging.info
import logging.verbose
import logging.warn
import transfer.TransferParamsParser.Result.Failed
import transfer.TransferPost.PATH

class TransferHandler(
    private val transferParamsParser: TransferParamsParser,
    private val clientRepository: ClientRepository,
    private val transferer: MoneyTransferer
) : BaseHandler {

    override fun attach(app: Javalin) {
        verbose { "Attaching TransferHandler" }
        app.post(PATH, this)
    }

    override fun handle(ctx: Context) {
        verbose { "Transfer requested with params: ${ctx.queryParamMap()}" }

        when (val result = transferParamsParser.parseParams(ctx)) {
            is Failed -> {
                ctx.status(400)
                ctx.result(result.message)
                warn { "Invalid params for transfer: ${ctx.queryString()}" }
                return
            }
            is TransferParamsParser.Result.Success -> {
                val request = result.params

                //check if client ids are valid
                val fromClient = clientRepository.getClientOrElse(request.fromClientId) {
                    warn { "Could not find a fromClient for id=${request.fromClientId}" }
                    ctx.status(400)
                    ctx.result("Could not find a valid client for id=${request.fromClientId}")
                    return
                }
                val toClient = clientRepository.getClientOrElse(request.toClientId) {
                    warn { "Could not find a toClient for id=${request.toClientId}" }
                    ctx.status(400)
                    ctx.result("Could not find a valid client for id=${request.toClientId}")
                    return
                }

                verbose { "Valid transfer request $request, transferring ${request.money} from $fromClient to $toClient" }
                when (val transferResult = transferer.transferMoney(request.money, from = fromClient, to = toClient)) {
                    is TransferResult.Success -> {
                        verbose { "Successfully completed transfer for request $request" }
                        ctx.status(200)
                        return
                    }
                    else -> {
                        info { "Failed to perform transfer for request $request: $transferResult" }
                        ctx.status(400)
                        ctx.result("Failed to perform transfer: $transferResult")
                        return
                    }
                }
            }
        }
    }
}

internal object TransferPost {

    const val PATH = "transfer"

    const val QUERY_FROM_ACCOUNT_ID = "from"
    const val QUERY_TO_ACCOUNT_ID = "to"
    const val QUERY_AMOUNT = "amount"
    const val QUERY_CURRENCY = "currency"

}