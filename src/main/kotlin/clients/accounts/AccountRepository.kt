package clients.accounts

import clients.Client
import java.util.*

interface AccountRepository {

    /**
     * Adds an account for the given client if they do not have an account with such a currency.
     * @return true if the account was added, or false when the client already has an account with the given currency
     * */
    fun addAccount(account: Account): Boolean

    /**
     * @return a set of all distinct accounts by currency for [client]. Can be empty.
     * */
    fun getAccounts(client: Client): Set<Account>

    /**
     * Gets [client]s Account for the given [currency], or null if [client] doesn't have an account for such a [currency]
     * */
    fun getAccount(client: Client, currency: Currency): Account?

}

inline fun AccountRepository.getAccountOrElse(
    client: Client,
    currency: Currency,
    whenNoAccount: () -> Account
): Account {
    return this.getAccount(client, currency) ?: whenNoAccount()
}

class InMemoryAccountRepository : AccountRepository {

    private val clientIdsToAccounts = mutableMapOf<Long, MutableMap<Currency, Account>>()

    override fun addAccount(account: Account): Boolean {
        val currency = account.currency
        val accountsForClient = clientIdsToAccounts.getOrPut(account.client.id) { mutableMapOf() }
        return if (currency in accountsForClient) {
            //account with given currency already exists
            false
        } else {
            accountsForClient[currency] = account
            true
        }
    }

    override fun getAccounts(client: Client): Set<Account> {
        return clientIdsToAccounts[client.id]?.values?.toSet() ?: emptySet()
    }

    override fun getAccount(client: Client, currency: Currency): Account? {
        return clientIdsToAccounts[client.id]?.get(currency)
    }
}