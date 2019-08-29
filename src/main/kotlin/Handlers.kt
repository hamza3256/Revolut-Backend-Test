import clients.ClientCreator
import clients.ClientHandler
import clients.ClientRepository
import money.account.AccountCreator
import money.account.AccountHandler
import transfer.MoneyTransferer
import transfer.TransferHandler
import transfer.TransferParamsParser

class Handlers(
    transferParamsParser: TransferParamsParser,
    clientRepository: ClientRepository,
    clientCreator: ClientCreator,
    transferer: MoneyTransferer,
    accountCreator: AccountCreator
) {

    private val transfer = TransferHandler(transferParamsParser, clientRepository, transferer)
    private val client = ClientHandler(clientCreator)
    private val account = AccountHandler(accountCreator, clientRepository)

    val all = listOf(transfer, client, account)

}