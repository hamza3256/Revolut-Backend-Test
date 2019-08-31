import kong.unirest.ObjectMapper
import kong.unirest.Unirest
import logging.debug

//maps a jackson ObjectMapper to a Unireset ObjectMapper
private fun com.fasterxml.jackson.databind.ObjectMapper.toUnirestObjectMapper(): ObjectMapper {
    return object : ObjectMapper {
        override fun writeValue(value: Any?): String {
            debug { "-> writeValue=$value" }
            return writeValueAsString(value).also {
                debug { "<- writeValue=$it" }
            }
        }

        override fun <T : Any?> readValue(value: String?, valueType: Class<T>?): T {
            debug { "-> readValue=$value" }
            return this@toUnirestObjectMapper.readValue(value, valueType).also {
                debug { "<- readValue=$it" }
            }
        }
    }
}

object UnirestTestConfig {

    fun init() {
        Unirest.config().objectMapper = RevolutJavalinConfig.objectMapper.toUnirestObjectMapper()
    }

    fun shutdown() {
        Unirest.shutDown()
    }
}
