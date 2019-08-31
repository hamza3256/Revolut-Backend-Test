package clients

import BaseHandler
import clients.CreateClient.ResponseBody
import clients.CreateClientHandler.Result.Success
import io.javalin.Javalin
import io.javalin.http.Context
import logging.verbose
import org.eclipse.jetty.http.HttpStatus.OK_200

class CreateClientHandler(private val clientCreator: ClientCreator) : BaseHandler {

    override fun attach(app: Javalin) {
        verbose { "Attaching CreateClientHandler" }
        app.post(CreateClient.PATH, this)
    }

    override fun handle(ctx: Context) {
        val result = handleWithResult(ctx)
        //Note: currently we only ever return Success, when adding other types of responses,
        //we should make handleWithResult() return Result, and handle different result types here.
        with(ctx) {
            status(result.statusCode)
            json(result.responseBody)
        }
    }

    private fun handleWithResult(ctx: Context): Success {
        verbose { "Requested to create new Client" }
        val body = ctx.body<CreateClient.RequestBody>()
        val createClientRequest = ClientCreator.Request(name = body.name, surname = body.surname)
        val client = clientCreator.create(createClientRequest)
        verbose { "Created $client for $body" }
        return Success(ResponseBody(client))
    }


    sealed class Result(val statusCode: Int) {

        data class Success(val responseBody: ResponseBody) : Result(OK_200)

    }
}

object CreateClient {

    const val PATH = "clients"

    data class RequestBody(val name: String, val surname: String)
    data class ResponseBody(val client: Client)

}