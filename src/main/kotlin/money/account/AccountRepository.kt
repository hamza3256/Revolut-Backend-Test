package money.account

import clients.Client
import java.util.*

interface AccountRepository {

    /**
     * Creates an account for the given client if they do not have an account with such a currency.
     * @return false when the client already has an account with the given currency, true otherwise
     * */
    fun addAccount(client: Client, account: Account): Boolean

    fun getAccounts(client: Client): List<Account>

    /**
     * Gets [client]s Account for the given [currency], or null if [client] doesn't have an account for such a [currency]
     * */
    fun getAccount(client: Client, currency: Currency): Account?

}

class InMemoryAccountRepository : AccountRepository {

    private val clientIdsToAccounts = mutableMapOf<Long, MutableMap<Currency, Account>>()

    override fun addAccount(client: Client, account: Account): Boolean {
        val currency = account.currency
        val accountsForClient = clientIdsToAccounts.getOrPut(client.id) { mutableMapOf() }
        return if (currency in accountsForClient) {
            //account with given currency already exists
            false
        } else {
            accountsForClient[currency] = account
            true
        }
    }

    override fun getAccounts(client: Client): List<Account> {
        return clientIdsToAccounts[client.id]?.values?.toList() ?: emptyList()
    }

    override fun getAccount(client: Client, currency: Currency): Account? {
        return clientIdsToAccounts[client.id]?.get(currency)
    }

/*
    override fun getAccount(client: Client): Account {
        return transactionRepository.getAll(client).let { transactions ->
            val moneyInEachCurrency: Map<Currency, BigDecimal> = transactions.filter { it.to == client }
                .groupBy(
                    keySelector = { transaction -> transaction.money.currency },
                    valueTransform = { transaction -> transaction.money.amount }
                )
                .mapValues {
                    it.value.sum()
                }

            Account(moneyInEachCurrency)
        }
    }
*/
}