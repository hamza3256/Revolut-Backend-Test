package clients

import logging.info

interface ClientRepository {

    /**
     * Adds a [client] to the repository if a client with the given id doesn't exist in the repository
     * @return true if the client didn't exist and was added, false if it already exists.
     */
    fun addClient(client: Client): Boolean

    /**
     * Gets a [Client] from the repository for the given [id]. If a client with [id] does not exist, it returns null
     * @return a [Client] with the given [id] or null if there is no such [Client]
     * */
    fun getClient(id: Long): Client?

    /**
     * Delete all Clients, but does not delete corresponding Accounts or Transactions
     * */
    fun deleteAll()

}

/**
 * An [ClientRepository] which stores [Client] in memory.
 * Project requirements state to use an in-memory datastore. Usually it would be persisted into an SQL database or similar
 * */
class InMemoryClientRepository : ClientRepository {
    private val idsToClient = mutableMapOf<Long, Client>()

    override fun addClient(client: Client): Boolean {
        return if (client.id in idsToClient) {
            info { "$client is already present in the repository" }
            false
        } else {
            idsToClient[client.id] = client
            info { "$client added to the repository" }
            true
        }
    }

    override fun getClient(id: Long): Client? = idsToClient[id]

    override fun deleteAll() = idsToClient.clear()
}

inline fun ClientRepository.getClientOrElse(id: Long, whenNoSuchClient: (Long) -> Client): Client {
    return this.getClient(id) ?: whenNoSuchClient(id)
}