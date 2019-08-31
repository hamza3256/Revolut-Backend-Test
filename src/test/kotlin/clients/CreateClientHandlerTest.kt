package clients

import RevolutJavalinConfig
import UnirestTestConfig
import clients.CreateClient.RequestBody
import clients.CreateClient.ResponseBody
import io.javalin.Javalin
import kong.unirest.Unirest
import org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400
import org.eclipse.jetty.http.HttpStatus.OK_200
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test

class CreateClientHandlerTest {

    companion object {

        private const val URL = "http://localhost:7000/clients"

        private lateinit var app: Javalin

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            UnirestTestConfig.init()
            val clientRepository = InMemoryClientRepository()
            val clientCreator = ClientCreatorImpl(clientRepository)
            val handler = CreateClientHandler(clientCreator)
            app = RevolutJavalinConfig.app.start()
            handler.attach(app)
        }

        @JvmStatic
        @AfterClass
        fun afterClass() {
            UnirestTestConfig.shutdown()
            app.stop()
        }
    }

    @Test
    fun `should create correct Client when called with expected body`() {
        val response = Unirest.post(URL)
            .body(RequestBody(name = "Nikolay", surname = "Storonsky"))
            .asObject(ResponseBody::class.java)

        assertEquals(OK_200, response.status)
        with(response.body.client) {
            assertEquals("Nikolay", name)
            assertEquals("Storonsky", surname)
        }
    }

    @Test
    fun `should fail when invalid body given`() {
        val response = Unirest.post(URL)
            .body("{}")
            .asString()

        assertEquals(BAD_REQUEST_400, response.status)
    }
}