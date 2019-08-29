import io.javalin.Javalin
import io.javalin.http.Handler

interface BaseHandler : Handler {

    fun attach(app: Javalin)

}