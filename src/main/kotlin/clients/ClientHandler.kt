package clients

import BaseHandler
import io.javalin.Javalin
import io.javalin.http.Context
import logging.verbose

class ClientHandler(private val clientCreator: ClientCreator) : BaseHandler {

    override fun attach(app: Javalin) {
        verbose { "Attaching ClientHandler" }
        app.post(CreateClient.PATH, this)
    }

    override fun handle(ctx: Context) {
        verbose { "Requested to create new Client" }
        val request = ctx.body<ClientCreator.Request>()
        val client = clientCreator.create(request)
        verbose { "Created $client for $request" }
        ctx.status(200)
        ctx.json(client)
    }
}

private object CreateClient {

    const val PATH = "client"

}