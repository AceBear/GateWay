package hub.gateway.realm

class DataRealm {
    val Id = "hello-world"

    fun GetFuncs():Array<Func>{
        val funcGV = Func("get-value")
        return arrayOf(funcGV)
    }
}