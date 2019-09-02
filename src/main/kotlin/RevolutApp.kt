@file:JvmName("App")

import customers.*
import customers.accounts.*
import customers.accounts.transactions.InMemoryTransactionRepository
import customers.accounts.transactions.TransactionCreator
import customers.accounts.transactions.TransactionCreatorImpl
import customers.accounts.transactions.TransactionRepository
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.javalin.Javalin
import io.javalin.plugin.json.JavalinJackson
import org.slf4j.LoggerFactory
import transfer.CreateTransferHandler
import transfer.MoneyTransferer
import transfer.MoneyTransfererImpl
import utils.info

fun main() {
    RevolutApp()
}

class RevolutApp {

    private val logger = LoggerFactory.getLogger("RevolutApp")

    init {
        val javalin = RevolutConfig().javalin

        //customers
        val customerRepository: CustomerRepository = InMemoryCustomerRepository()
        val customerCreator: CustomerCreator = CustomerCreatorImpl(customerRepository)
        val createCustomerHandler = CreateCustomerHandler(customerCreator)
        createCustomerHandler.attach(javalin)

        //transactions
        val transactionRepository: TransactionRepository = InMemoryTransactionRepository()
        val transactionCreator: TransactionCreator = TransactionCreatorImpl(transactionRepository)

        //accounts
        val accountRepository: AccountRepository = InMemoryAccountRepository()
        val accountCreator: AccountCreator = AccountCreatorImpl(accountRepository)
        val accountStateQuerier: AccountStateQuerier = AccountStateQuerierImpl(transactionRepository)
        val createAccountHandler = CreateAccountHandler(accountCreator, customerRepository)
        createAccountHandler.attach(javalin)
        val accountStateHandler = GetAccountStateHandler(accountRepository, accountStateQuerier)
        accountStateHandler.attach(javalin)

        //transfer
        val moneyTransferer: MoneyTransferer = MoneyTransfererImpl(transactionCreator, accountStateQuerier)
        val createTransferHandler = CreateTransferHandler(accountRepository, moneyTransferer)
        createTransferHandler.attach(javalin)

        logger.info { "Starting server..." }
        javalin.start(7000)
    }
}

class RevolutConfig {

    //only serialize fields to JSON
    var objectMapper: ObjectMapper = ObjectMapper().registerKotlinModule()
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.NONE)
        .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
        .setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
        .setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)

    val javalin: Javalin = Javalin.create()

    init {
        JavalinJackson.configure(objectMapper)
    }
}
