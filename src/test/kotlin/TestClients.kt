import clients.Client

object Clients {

    /**
     * Created a new Client representing Vlad Yatsenko with id 0
     * */
    fun vlad(id: Long = 0): Client {
        return Client(id, "Vlad", "Yatsenko")
    }

    /**
     * Created a new Client representing Nikolay Storonsky with id 0
     * */
    fun nikolay(id: Long = 0): Client {
        return Client(id, "Nikolay", "Storonsky")
    }

}