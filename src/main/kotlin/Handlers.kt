import clients.ClientCreator
import clients.CreateClientHandler
import clients.ClientRepository
import clients.accounts.AccountCreator
import clients.accounts.CreateAccountHandler
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
    private val client = CreateClientHandler(clientCreator)
    private val account = CreateAccountHandler(accountCreator, clientRepository)

    val all = listOf(transfer, client, account)

}