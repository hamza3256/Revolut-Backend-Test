package clients

import logging.error
import logging.info
import java.util.concurrent.atomic.AtomicLong

interface ClientCreator {

    /**
     * A request to create a new [Client]. The [Client] created from this request will have the same name and surname.
     * */
    data class Request(val name: String, val surname: String)

    /**
     * Create a new [Client] for the given [request].
     *
     * @return the newly created Client
     * */
    fun create(request: Request): Client

}

class ClientCreatorImpl(private val clientRepository: ClientRepository) : ClientCreator {

    private val nextId = AtomicLong()

    override fun create(request: ClientCreator.Request): Client {
        with(request) {
            info { "Creating client for request $this" }
            return Client(
                id = nextId.getAndIncrement(),
                name = name,
                surname = surname
            ).also { client ->
                //insert into repository
                if (clientRepository.addClient(client).not()) {
                    //this shouldn't ever happen -> we create a new id each time we create a new client instance
                    error { "Failed to add client to repository for request=$request, client=$client" }
                    throw RuntimeException("Could not create client=$client as it already exists in the repository")
                }
            }
        }
    }
}