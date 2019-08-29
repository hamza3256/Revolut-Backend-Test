package transfer

import io.javalin.http.Context
import money.Money
import transfer.TransferParamsParser.Result.Failed
import transfer.TransferPost.QUERY_AMOUNT
import transfer.TransferPost.QUERY_CURRENCY
import transfer.TransferPost.QUERY_FROM_ACCOUNT_ID
import transfer.TransferPost.QUERY_TO_ACCOUNT_ID
import utils.requireParam
import utils.toLong
import java.math.BigDecimal
import java.util.*

class TransferParamsParser {

    fun parseParams(context: Context): Result {

        //TODO don't repeat the code :/
        //extract the params
        val fromAccountIdParam = context.requireParam(QUERY_FROM_ACCOUNT_ID) { param ->
            return Failed("Missing param=$param")
        }
        val toAccountIdParam = context.requireParam(QUERY_TO_ACCOUNT_ID) { param ->
            return Failed("Missing param=$param")
        }

        val amountParam = context.requireParam(QUERY_AMOUNT) { param ->
            return Failed("Missing param=$param")
        }
        val currencyParam = context.requireParam(QUERY_CURRENCY) { param ->
            return Failed("Missing param=$param")
        }

        //parse params to correct format
        val fromAccountId = fromAccountIdParam.toLong {
            return Failed("$QUERY_FROM_ACCOUNT_ID=$fromAccountIdParam is not a valid long")
        }

        val toAccountId = toAccountIdParam.toLong {
            return Failed("$QUERY_TO_ACCOUNT_ID=$toAccountIdParam is not a valid long")
        }

        val amount = amountParam.toBigDecimal {
            return Failed("$QUERY_AMOUNT=$amountParam is not a valid BigDecimal")
        }

        val currency = currencyParam.toCurrency {
            //incorrect currency code
            return Failed("$QUERY_CURRENCY=$currencyParam is not a valid ISO 4217 currency code")
        }

        //success, valid request
        val request = TransferRequest(
            fromClientId = fromAccountId,
            toClientId = toAccountId,
            money = Money(amount, currency)
        )

        return Result.Success(request)
    }

    sealed class Result {
        class Success(val params: TransferRequest) : Result()
        class Failed(val message: String) : Result()
    }
}

private inline fun String.toBigDecimal(whenNotBigDecimal: (String) -> BigDecimal): BigDecimal {
    return try {
        this.toBigDecimal()
    } catch (exception: NumberFormatException) {
        whenNotBigDecimal(this)
    }
}

private inline fun String.toCurrency(whenInvalidCurrencyCode: (String) -> Currency): Currency {
    return try {
        //Currency.getInstance() only returns null when we give it a null String, so it's never nullable here
        Currency.getInstance(this)!!
    } catch (exception: IllegalArgumentException) {
        return whenInvalidCurrencyCode(this)
    }
}