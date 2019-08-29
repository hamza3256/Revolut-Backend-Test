package clients

import org.junit.Assert.assertEquals
import org.junit.Test

class ClientCreatorImplTest {

    private val clientRepository = InMemoryClientRepository()
    private val clientCreator = ClientCreatorImpl(clientRepository)
    private val nikolayRequest = ClientCreator.Request(name = "Nikolay", surname = "Storonsky")

    @Test
    fun `creating a client with the same request should create a new client with a new id`() {
        val clients = (0..1).map { clientCreator.create(nikolayRequest) }
        val distinctById = clients.distinctBy { it.id }

        assertEquals(clients, distinctById)
    }

    @Test
    fun `created client should have the same name and surname as the quest`() {
        val client = clientCreator.create(nikolayRequest)

        assertEquals(nikolayRequest.name, client.name)
        assertEquals(nikolayRequest.surname, client.surname)
    }

    @Test
    fun `created client should be inserted into the repository`() {
        val client = clientCreator.create(nikolayRequest)
        val clientFromRepository = clientRepository.getClient(client.id)

        assertEquals(client, clientFromRepository)
    }
}