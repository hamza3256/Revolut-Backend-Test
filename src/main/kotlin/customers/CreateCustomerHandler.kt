package customers

import BaseHandler
import customers.CreateCustomer.ResponseBody
import customers.CreateCustomerHandler.Result.Success
import io.javalin.Javalin
import io.javalin.http.Context
import org.eclipse.jetty.http.HttpStatus.OK_200
import org.slf4j.LoggerFactory
import utils.info

class CreateCustomerHandler(private val customerCreator: CustomerCreator) : BaseHandler {

    private val logger = LoggerFactory.getLogger("CreateCustomerHandler")

    override fun attach(app: Javalin) {
        logger.info { "Attaching CreateCustomerHandler" }
        app.post(CreateCustomer.PATH, this)
    }

    override fun handle(ctx: Context) {
        val result = handleWithResult(ctx)
        //Note: currently we only ever return Success, when adding other types of responses,
        //we should make handleWithResult() return Result, and handle different result types here.
        with(ctx) {
            status(result.statusCode)
            json(result.responseBody)
        }
    }

    private fun handleWithResult(ctx: Context): Success {
        logger.info { "Requested to create new Customer" }
        val body = ctx.body<CreateCustomer.RequestBody>()
        val createCustomerRequest = CustomerCreator.Request(name = body.name, surname = body.surname)
        val customer = customerCreator.create(createCustomerRequest)
        logger.info { "Created $customer for $body" }
        return Success(ResponseBody(customer))
    }


    sealed class Result(val statusCode: Int) {

        data class Success(val responseBody: ResponseBody) : Result(OK_200)

    }
}

object CreateCustomer {

    const val PATH = "customers"

    data class RequestBody(val name: String, val surname: String)
    data class ResponseBody(val customer: Customer)

}