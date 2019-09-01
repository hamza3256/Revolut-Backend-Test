package clients.accounts

import clients.Client
import java.util.*

interface AccountRepository {

    /**
     * Adds an account for the given client if they do not have an account with such a currency.
     * @return true if an account with such an id didn't exist, and the account was added, or false when an account already exists with the id and wasn't added
     * */
    fun addAccount(account: Account): Boolean

    /**
     * @return a set of all accounts belonging to [client]. Can be empty.
     * */
    fun getAccounts(client: Client): Set<Account>

    /**
     * Gets an [Account] for [id], or null if there is no such [Account]
     * */
    fun getAccount(id: Long): Account?

    /**
     * Delete all Accounts. Does not delete any transactions for the Accounts.
     * */
    fun deleteAll()

}

inline fun AccountRepository.getAccountOrElse(
    accountId: Long,
    whenNoAccount: () -> Account
): Account {
    return this.getAccount(accountId) ?: whenNoAccount()
}

class InMemoryAccountRepository : AccountRepository {

    private val idsToAccount = mutableMapOf<Long, Account>()

    override fun addAccount(account: Account): Boolean {
        return if (account.id in idsToAccount) {
            //account already exists with this id
            false
        } else {
            idsToAccount[account.id] = account
            true
        }
    }

    override fun getAccounts(client: Client): Set<Account> {
        return idsToAccount.values.filter{ it.client == client }.toSet()
    }

    override fun getAccount(id: Long): Account? {
        return idsToAccount[id]
    }

    override fun deleteAll() = idsToAccount.clear()
}