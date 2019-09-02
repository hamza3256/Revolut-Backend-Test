package customers

data class Customer internal constructor(
    val id: Long,
    val name: String,
    val surname: String
)