package customers.accounts

import customers.Customer

interface AccountRepository {

    /**
     * Adds an account for the given customer if they do not have an account with such a currency.
     * @return true if an account with such an id didn't exist, and the account was added, or false when an account already exists with the id and wasn't added
     * */
    fun addAccount(account: Account): Boolean

    /**
     * @return a set of all accounts belonging to [customer]. Can be empty.
     * */
    fun getAccounts(customer: Customer): Set<Account>

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

    override fun getAccounts(customer: Customer): Set<Account> {
        return idsToAccount.values.filter{ it.customer == customer }.toSet()
    }

    override fun getAccount(id: Long): Account? {
        return idsToAccount[id]
    }

    override fun deleteAll() = idsToAccount.clear()
}