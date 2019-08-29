package clients

import clients.ClientCreator.Result
import clients.ClientCreator.Result.AlreadyCreated
import logging.error
import logging.info
import java.util.concurrent.atomic.AtomicLong

interface ClientCreator {

    /**
     * A request to create a new [Client]. The [Client] created from this request will have the same name and surname.
     * The [token] must be a unique String per request.
     * */
    data class Request(val name: String, val surname: String, val token: String)

    /**
     * Create a new [Client] for the given [request].
     * If a client has previously been created with the given token, then it does not createTransferTransactions one and instead returns the previously created Client
     *
     * @return the newly created Client
     * */
    fun create(request: Request): Result

    sealed class Result {

        class Success(val client: Client) : Result()
        class AlreadyCreated(val client: Client) : Result()

    }
}

class ClientCreatorImpl(private val clientRepository: ClientRepository) : ClientCreator {

    private val nextId = AtomicLong()
    private val tokensToHandledCreateRequest = mutableMapOf<String, HandledCreateRequest>()

    override fun create(request: ClientCreator.Request): Result {
        with(request) {
            val handledCreateRequest = tokensToHandledCreateRequest[token]
            if (handledCreateRequest?.request == request) {
                //we've already handled this request, don't create a duplicate Client
                info { "Requested to create client using $request but a Client has already been created" }
                val client = handledCreateRequest.client
                return AlreadyCreated(client)
            }

            info { "Creating client for request $request" }
            return Client(
                id = nextId.getAndIncrement(),
                name = name,
                surname = surname
            ).let { client ->
                if (clientRepository.addClient(client)) {
                    tokensToHandledCreateRequest[token] = HandledCreateRequest(request, client)
                    Result.Success(client)
                } else {
                    //this shouldn't ever happen -> we create a new id each time we create a new client instance
                    error { "Failed to add client to repository for request=$request, client=$client" }
                    throw RuntimeException("Could not create client=$client as it already exists in the repository")
                }
            }
        }
    }

    data class HandledCreateRequest(val request: ClientCreator.Request, val client: Client)
}