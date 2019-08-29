import clients.ClientCreator
import clients.ClientCreatorImpl
import clients.ClientRepository
import clients.InMemoryClientRepository
import io.javalin.Javalin
import logging.info
import money.account.AccountCreator
import money.account.AccountCreatorImpl
import money.account.AccountRepository
import money.account.InMemoryAccountRepository
import money.transactions.InMemoryTransactionRepository
import money.transactions.TransactionCreator
import money.transactions.TransactionCreatorImpl
import money.transactions.TransactionRepository
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

        //accounts
        val accountRepository: AccountRepository = InMemoryAccountRepository()
        val accountCreator: AccountCreator = AccountCreatorImpl(accountRepository)

        //transactions
        val transactionRepository: TransactionRepository = InMemoryTransactionRepository()
        val transactionCreator: TransactionCreator = TransactionCreatorImpl(transactionRepository)

        //transfer
        val transferParamsParser = TransferParamsParser()
        val moneyTransferer: MoneyTransferer = MoneyTransfererImpl(accountRepository, transactionCreator)

        val handlers = Handlers(transferParamsParser, clientRepository, clientCreator, moneyTransferer, accountCreator)

        info { "Starting server..." }
        val app = Javalin.create().start(7000)

        handlers.all.forEach { handler ->
            handler.attach(app)
        }
    }
}