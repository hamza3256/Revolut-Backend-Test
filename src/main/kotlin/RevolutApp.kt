@file:JvmName("App")

import customers.*
import customers.accounts.*
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import customers.accounts.transactions.*
import io.javalin.Javalin
import io.javalin.plugin.json.JavalinJackson
import money.Money
import org.slf4j.LoggerFactory
import transfer.CreateTransferHandler
import transfer.MoneyTransferer
import transfer.MoneyTransfererImpl
import utils.debug
import utils.info
import java.util.*

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

        //to make it easier to show everything works, we create some Accounts with GBP, EUR, USD
        addDebugBankCustomer(customerCreator, accountCreator, transactionRepository)

        logger.info { "Starting server..." }
        javalin.start(7000)
    }

    private fun addDebugBankCustomer(customerCreator: CustomerCreator, accountCreator: AccountCreator, transactionRepository: TransactionRepository) {
        //we don't have an API for depositing money into an Account, so to bypass this add 3 Accounts with USD, GBP, and EUR which we can transfer money from
        logger.debug { "ADDING DEBUG BANK CUSTOMER & ACCOUNTS" }
        //Add a 'Bank' Customer & Account with 1 million EUR, USD, GBP
        val bank = customerCreator.create(CustomerCreator.Request(name = "Bank", surname = "Revolut"))

        val usdAccount = accountCreator.create(bank, AccountCreator.Request(Currency.getInstance("USD"))) //id is 0
        val gbpAccount = accountCreator.create(bank, AccountCreator.Request(Currency.getInstance("GBP"))) //id is 1
        val eurAccount = accountCreator.create(bank, AccountCreator.Request(Currency.getInstance("EUR"))) //id is 2

        //transactionCreator starts IDs from 0, since we bypass that class - use negative ids to avoid collisions
        transactionRepository.add(Transaction(id = -1, account = usdAccount, money = Money(1_000_000.toBigDecimal(), Currency.getInstance("USD"))))
        transactionRepository.add(Transaction(id = -2, account = gbpAccount, money = Money(1_000_000.toBigDecimal(), Currency.getInstance("GBP"))))
        transactionRepository.add(Transaction(id = -3, account = eurAccount, money = Money(1_000_000.toBigDecimal(), Currency.getInstance("EUR"))))
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
