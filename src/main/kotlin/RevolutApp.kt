import clients.ClientCreator
import clients.ClientCreatorImpl
import clients.ClientRepository
import clients.InMemoryClientRepository
import clients.accounts.*
import clients.transactions.InMemoryTransactionRepository
import clients.transactions.TransactionCreator
import clients.transactions.TransactionCreatorImpl
import clients.transactions.TransactionRepository
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.javalin.Javalin
import io.javalin.plugin.json.JavalinJackson
import logging.info
import transfer.MoneyTransferer
import transfer.MoneyTransfererImpl
import transfer.TransferParamsParser


fun main() {
    RevolutApp()
}

class RevolutApp {

    init {
        //clients
        val clientRepository: ClientRepository = InMemoryClientRepository()
        val clientCreator: ClientCreator = ClientCreatorImpl(clientRepository)

        //transactions
        val transactionRepository: TransactionRepository = InMemoryTransactionRepository()
        val transactionCreator: TransactionCreator = TransactionCreatorImpl(transactionRepository)

        //accounts
        val accountRepository: AccountRepository = InMemoryAccountRepository()
        val accountCreator: AccountCreator = AccountCreatorImpl(accountRepository)
        val accountStateQuerie: AccountStateQuerier = AccountStateQuerierImpl(transactionRepository)

        //transfer
        val transferParamsParser = TransferParamsParser()
        val moneyTransferer: MoneyTransferer =
            MoneyTransfererImpl(accountRepository, transactionCreator, accountStateQuerie)

        //TODO remove Handlers object
        val handlers = Handlers(transferParamsParser, clientRepository, clientCreator, moneyTransferer, accountCreator)

        info { "Starting server..." }
        val app = RevolutJavalinConfig.app.start(7000)

        handlers.all.forEach { handler ->
            handler.attach(app)
        }
    }
}

object RevolutJavalinConfig {

    //only serialize fields to JSON
    var objectMapper: ObjectMapper = ObjectMapper().registerKotlinModule()
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.NONE)
        .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
        .setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
        .setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)

    val app: Javalin by lazy { Javalin.create() }

    init {
        JavalinJackson.configure(objectMapper)
    }
}
