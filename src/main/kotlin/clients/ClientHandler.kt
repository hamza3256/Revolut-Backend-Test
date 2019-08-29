package clients

import BaseHandler
import io.javalin.Javalin
import io.javalin.http.Context
import logging.info
import logging.verbose

class ClientHandler(private val clientCreator: ClientCreator) : BaseHandler {

    override fun attach(app: Javalin) {
        verbose { "Attaching ClientHandler" }
        app.post(CreateClient.PATH, this)
    }

    override fun handle(ctx: Context) {
        val request = ctx.body<ClientCreator.Request>()
        when (val result = clientCreator.create(request)) {
            is ClientCreator.Result.Success -> {
                info { "Client created as $result" }
                ctx.status(200)
                ctx.json(result.client)
            }
            is ClientCreator.Result.AlreadyCreated -> {
                info { "Client already exists" }
                ctx.status(200)
                ctx.json(result.client)
            }
        }
    }
}

private object CreateClient {

    const val PATH = "client"

}