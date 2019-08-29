package clients

data class Client internal constructor(
    val id: Long,
    val name: String,
    val surname: String
)