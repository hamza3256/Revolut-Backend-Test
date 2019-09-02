import kong.unirest.ObjectMapper
import kong.unirest.Unirest

//maps a jackson ObjectMapper to a Unireset ObjectMapper
private fun com.fasterxml.jackson.databind.ObjectMapper.toUnirestObjectMapper(): ObjectMapper {
    return object : ObjectMapper {
        override fun writeValue(value: Any?): String {
            return writeValueAsString(value)
        }

        override fun <T : Any?> readValue(value: String?, valueType: Class<T>?): T {
            return this@toUnirestObjectMapper.readValue(value, valueType)
        }
    }
}

object UnirestTestConfig {

    fun init(objectMapper: com.fasterxml.jackson.databind.ObjectMapper) {
        Unirest.config().objectMapper = objectMapper.toUnirestObjectMapper()
    }

    fun shutdown() {
        Unirest.shutDown()
    }
}
