package clients

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InMemoryClientRepositoryTest {

    private lateinit var repository: ClientRepository

    @Before
    fun beforeEachTest() {
        repository = InMemoryClientRepository()
    }

    @Test
    fun `adding a client should imply that you can get it`() {
        val client = Clients.nikolay(id = 0)

        //shouldn't exist yet
        assertNull(repository.getClient(0))

        assertTrue(repository.addClient(client))

        assertEquals(client, repository.getClient(0))
    }

    @Test
    fun `adding a client with the same id should return false`() {
        val client = Clients.nikolay(id = 0)
        val clientWithSameId = Clients.vlad(id = 0)

        assertTrue(repository.addClient(client))
        assertFalse(repository.addClient(clientWithSameId))
    }

    @Test
    fun `adding a client with the same id shouldn't replace the original client`(){
        val client = Clients.nikolay(id = 0)
        assertTrue(repository.addClient(client))

        val clientWithSameId = Clients.vlad(id = 0)
        assertFalse(repository.addClient(clientWithSameId))

        //shouldn't have been replaced
        assertEquals(client, repository.getClient(0))
    }

    @Test
    fun `getting a client for an invalid id should return null`() {
        assertNull(repository.getClient(0))
    }
}