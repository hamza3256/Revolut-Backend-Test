import clients.ClientCreator
import clients.ClientCreatorImpl
import clients.ClientRepository
import clients.InMemoryClientRepository
import io.javalin.Javalin
import logging.info
import clients.accounts.*
import clients.transactions.InMemoryTransactionRepository
import clients.transactions.TransactionCreator
import clients.transactions.TransactionCreatorImpl
import clients.transactions.TransactionRepository
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
        val moneyTransferer: MoneyTransferer = MoneyTransfererImpl(accountRepository, transactionCreator, accountStateQuerie)

        val handlers = Handlers(transferParamsParser, clientRepository, clientCreator, moneyTransferer, accountCreator)

        info { "Starting server..." }
        val app = Javalin.create().start(7000)

        handlers.all.forEach { handler ->
            handler.attach(app)
        }
    }
}